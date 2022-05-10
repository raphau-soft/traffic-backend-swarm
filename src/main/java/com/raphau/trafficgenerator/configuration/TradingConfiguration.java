package com.raphau.trafficgenerator.configuration;

import com.raphau.trafficgenerator.service.AsyncService;
import com.raphau.trafficgenerator.service.RunTestService;
import io.swagger.models.auth.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class TradingConfiguration implements SchedulingConfigurer {

    @Bean
    public Executor taskExecutor()  {
        return Executors.newSingleThreadScheduledExecutor();
    }

    private static final Logger log = LoggerFactory.getLogger(TradingConfiguration.class);

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private RabbitAdmin admin;

    @Autowired
    private List<Queue> rabbitQueues;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
        taskRegistrar.addTriggerTask(
                () -> {
                    if (!RunTestService.testRunning || asyncService.isEndWork()) return;
                    AsyncService.trading = true;
                    int messageCount = getMessageCount();
                    log.info("Trying to send trade tick...");
                    while (messageCount > 0) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        messageCount = getMessageCount();
                        log.info("Offers while processing: " + messageCount);
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    log.info("Offers processed - sending trade tick");
                    int validate = RunTestService.validator = 0;
                    rabbitTemplate.convertAndSend("trade-request-exchange", "foo.bar.#", "0");
                    while (AsyncService.trading) {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (validate != RunTestService.validator) {
                            AsyncService.trading = false;
                            log.info("Stock exchange failed to send confirmation, continuing...");
                        }
                        validate++;
                    }
                },
                context -> {
                    Optional<Date> lastCompletionTime =
                            Optional.ofNullable(context.lastCompletionTime());
                    Instant nextExecutionTime =
                            lastCompletionTime.orElseGet(Date::new).toInstant()
                                    .plusMillis(RunTestService.runTestDTO.getBreakBetweenTrades() * 1000L);
                    return Date.from(nextExecutionTime);
                }
        );

    }

    private int getMessageCount() {
        Properties props;
        int count = 0;
        for (Queue queue : rabbitQueues) {
            props = admin.getQueueProperties(queue.getName());
            assert props != null;
            count += Integer.parseInt(props.get("QUEUE_MESSAGE_COUNT").toString());
        }
        return count;
    }
}
