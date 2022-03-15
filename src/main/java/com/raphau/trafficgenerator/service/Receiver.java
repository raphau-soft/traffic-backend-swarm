package com.raphau.trafficgenerator.service;

import com.raphau.trafficgenerator.controller.TestController;
import com.raphau.trafficgenerator.dao.StockExchangeCpuDataRepository;
import com.raphau.trafficgenerator.dao.StockExchangeTimeDataRepository;
import com.raphau.trafficgenerator.dao.TestRepository;
import com.raphau.trafficgenerator.dao.TrafficGeneratorTimeDataRepository;
import com.raphau.trafficgenerator.dto.*;
import com.raphau.trafficgenerator.entity.StockExchangeCpuData;
import com.raphau.trafficgenerator.entity.StockExchangeTimeData;
import com.raphau.trafficgenerator.entity.Test;
import com.raphau.trafficgenerator.entity.TrafficGeneratorTimeData;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
public class Receiver {

    Logger logger = LoggerFactory.getLogger(Receiver.class);

    @Autowired
    private StockExchangeCpuDataRepository stockExchangeCpuDataRepository;

    @Autowired
    private StockExchangeTimeDataRepository stockExchangeTimeDataRepository;

    @Autowired
    private TrafficGeneratorTimeDataRepository trafficGeneratorTimeDataRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private List<Queue> rabbitQueues;

    @Autowired
    private RabbitAdmin admin;

    @RabbitListener(queues = "test-details-response")
    @Transactional
    public void receiveOfferMessage(TrafficGeneratorTimeDataDTO trafficGeneratorTimeDataDTO) {
        if(!TestController.testRunning) return;
        // traffic generator request time data
        Test test = testRepository.findById(TestController.test.getId()).get();
        TrafficGeneratorTimeData trafficGeneratorTimeData = new TrafficGeneratorTimeData(trafficGeneratorTimeDataDTO, test);
        trafficGeneratorTimeDataRepository.save(trafficGeneratorTimeData);
    }

    @RabbitListener(queues = "cpu-data-request")
    public void receiveCpuDataMessage(CpuDataDTO cpuDataDTO) {
        if(!TestController.testRunning) return;
        Test test = testRepository.findById(TestController.test.getId()).get();
        StockExchangeCpuData stockExchangeCpuData = new StockExchangeCpuData(cpuDataDTO, test);
        stockExchangeCpuDataRepository.save(stockExchangeCpuData);
        System.out.println(stockExchangeCpuData);
    }

    @RabbitListener(queues = "time-data-request")
    public void receiveTimeDataMessage(TimeDataDTO timeDataDTO) {
        if(!TestController.testRunning) return;
        // transaction time data
        Test test = testRepository.findById(TestController.test.getId()).get();
        StockExchangeTimeData stockExchangeTimeData = new StockExchangeTimeData(timeDataDTO, test);
        stockExchangeTimeDataRepository.save(stockExchangeTimeData);
        System.out.println(stockExchangeTimeData);
    }

    @RabbitListener(queues = "stock-data-response")
    public void receiveStockDataMessage(Map<String, Object> objects) {
        int user = Integer.parseInt((String) objects.get("username"));
        asyncService.getStockData().set(user, new JSONObject(objects));
        AsyncService.semaphores.get(user).release();
    }

    @RabbitListener(queues = "user-data-response")
    public void receiveUserDataMessage(Map<String, Object> objects) {
        int user = Integer.parseInt((String) objects.get("username"));
        asyncService.getUsersAndCompanies().set(user, new JSONObject(objects));
        AsyncService.semaphores.get(user).release();
    }

    @RabbitListener(queues = "register-response")
    public void receiveRegisterMessage(String flag) {
        TestController.register.release();
    }

    @RabbitListener(queues = "trade-response")
    public void receiveTradeMessage(String tick) {
        logger.info("Received trade response tick");
        AsyncService.trading = false;
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void trade() throws InterruptedException {
        AsyncService.trading = true;
//        int messageCount =
//                getMessageCount("buy-offer-request") +
//                getMessageCount("sell-offer-request") +
//                getMessageCount("company-request");
        int messageCount = getMessageCount();
        logger.info("Waiting for offers to process...");
        while(messageCount > 0) {
            Thread.sleep(3000);
//            messageCount = getMessageCount("buy-offer-request") +
//                    getMessageCount("sell-offer-request") +
//                    getMessageCount("company-request");
            messageCount = getMessageCount();
            logger.info("Offers while processing: " + messageCount);
        }
        Thread.sleep(10000);
        logger.info("Offers processed - sending trade tick");
        this.rabbitTemplate.convertAndSend("trade-request-exchange", "foo.bar.#", "tick");
    }

    private int getMessageCount(String name) {
        Properties props;
        props = admin.getQueueProperties(name);
        return Integer.parseInt(props.get("QUEUE_MESSAGE_COUNT").toString());
    }

    private int getMessageCount() {
        Properties props;
        int count = 0;
        for(Queue queue : rabbitQueues) {
            props = admin.getQueueProperties(queue.getName());
            count += Integer.parseInt(props.get("QUEUE_MESSAGE_COUNT").toString());
        }
        return count;
    }

}
