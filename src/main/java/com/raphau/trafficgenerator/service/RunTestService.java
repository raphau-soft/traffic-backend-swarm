package com.raphau.trafficgenerator.service;

import com.raphau.trafficgenerator.controller.TestController;
import com.raphau.trafficgenerator.dao.TestParametersRepository;
import com.raphau.trafficgenerator.dao.TestRepository;
import com.raphau.trafficgenerator.dao.TrafficGeneratorCpuDataRepository;
import com.raphau.trafficgenerator.dto.RunTestDTO;
import com.raphau.trafficgenerator.dto.TestDTO;
import com.raphau.trafficgenerator.entity.Test;
import com.raphau.trafficgenerator.entity.TestParameters;
import com.raphau.trafficgenerator.entity.TrafficGeneratorCpuData;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Semaphore;

@Service
public class RunTestService {
    private static final Logger log = LoggerFactory.getLogger(TestController.class);
    public static RunTestDTO runTestDTO = new RunTestDTO(1000, 50, 1800, 80, 50, 0, 0, false, true);
    private final OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
    public static int processNumber = 0;
    public static Semaphore register;
    public static Semaphore trade = new Semaphore(1);
    public static boolean testRunning = false;
    public static TestDTO testDTO;
    public static int registered;
    public static int validated;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private TestParametersRepository testParametersRepository;

    @Autowired
    private TrafficGeneratorCpuDataRepository cpuDataRepository;

    @Autowired
    private RabbitAdmin admin;

    @Autowired
    private List<Queue> rabbitQueues;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void asyncTest() throws Exception {
        log.info("Test is starting");
        registered = 0;
        Test test = new Test(0, generateTestName(), System.currentTimeMillis(), null, false);
        TestParameters testParameters = new TestParameters(runTestDTO);
        testRepository.saveAndFlush(test);
        test.setTestParameters(testParameters);
        testParameters.setTest(test);
        testParametersRepository.saveAndFlush(testParameters);
        testDTO = new TestDTO(test);

        asyncService.setEndWork(false);
        log.info("Purging all queues...");
        for(Queue queue: rabbitQueues) {
            admin.purgeQueue(queue.getName());
        }
        List<JSONObject> objects = Collections.synchronizedList(new ArrayList<>());
        List<JSONObject> users = Collections.synchronizedList(new ArrayList<>());
        List<Semaphore> semaphores = Collections.synchronizedList(new ArrayList<>());
        objects.add(null);
        users.add(null);
        semaphores.add(null);
        register = new Semaphore(1);
        asyncService.clearStockDB();
        for(int i = 1; i <= runTestDTO.getNumberOfUsers(); i++){
            objects.add(null);
            users.add(null);
            semaphores.add(new Semaphore(1));
            asyncService.postRegistration(""+i);
        }

        log.info("Waiting for all user's to register, users: " + runTestDTO.getNumberOfUsers() + " registered: " + registered);
        while(registered < runTestDTO.getNumberOfUsers()) {
            Thread.sleep(3000);
            log.info("Waiting for all user's to register, users: " + runTestDTO.getNumberOfUsers() + " registered: " + registered);
        }

        asyncService.setStockData(objects);
        asyncService.setUsersAndCompanies(users);
        AsyncService.semaphores = semaphores;

        for(int i = 1; i <= runTestDTO.getFirst(); i++){
            asyncService.runTests("" + i, runTestDTO, 1);
            processNumber++;
        }
        for(int i = 1; i <= runTestDTO.getSecond(); i++){
            asyncService.runTests("" + i, runTestDTO, 2);
            processNumber++;
        }
        for(int i = 1; i <= runTestDTO.getThird(); i++){
            asyncService.runTests("" + i, runTestDTO, 3);
            processNumber++;
        }
        testRunning = true;
    }

    @Scheduled(cron = "0 */1 * * * ?")
    @Transactional
    public void checkTestStatus() {
        if(!testRunning || AsyncService.trading) return;
        collectCpuData();
        if(runTestDTO.isTimeLimit()) {
            long currentTime = System.currentTimeMillis();
            long testEndTime = testDTO.getStartTimestamp().getTime() + testDTO.getTestTime() * 1000L;
            if(currentTime < testEndTime) return;
            asyncService.setEndWork(true);
        }
        if(processNumber > 0) return;
        log.info("Test is going to be finished...");
        Optional<Test> testOptional = testRepository.findById(testDTO.getId());
        if(!testOptional.isPresent()) {
            testRunning = false;
            return;
        }
        Test test = testOptional.get();
        test.setEndTimestamp(System.currentTimeMillis());
        test.setFinished(true);
        testRepository.save(test);
        testRunning = false;
    }

    public void collectCpuData() {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.getName().startsWith("getSystem")
                    && Modifier.isPublic(method.getModifiers())) {
                Object value;
                try {
                    value = method.invoke(bean);
                } catch (Exception e) {
                    value = e;
                }

                Optional<Test> testOptional = testRepository.findById(testDTO.getId());
                if(!testOptional.isPresent()) return;
                Test test = testOptional.get();
                TrafficGeneratorCpuData cpuData = new TrafficGeneratorCpuData(0, test, System.currentTimeMillis(), (Double) value);
                cpuDataRepository.save(cpuData);
            }
        }
    }

    public void stopTest() throws InterruptedException {
        asyncService.setEndWork(true);
        log.info("Waiting for test to end. Active users: " + processNumber);
        while (processNumber != 0) {
            Thread.sleep(3000);
            log.info("Waiting for test to end. Active users: " + processNumber);
        }
        int messageCount = getMessageCount();
        log.info("Waiting for offers to process...");
        while(messageCount > 0) {
            Thread.sleep(3000);
            messageCount = getMessageCount();
            log.info("Offers while processing: " + messageCount);
        }
        Optional<Test> testOptional = testRepository.findById(testDTO.getId());
        if(!testOptional.isPresent()) {
            testRunning = false;
            asyncService.setEndWork(true);
            return;
        }
        Test test = testOptional.get();
        test.setFinished(true);
        test.setEndTimestamp(System.currentTimeMillis());
        testRepository.save(test);
        testRunning = false;
        log.info("Test finished");
    }

    @Scheduled(fixedDelay = 300000)
    public void trade() throws InterruptedException {
        if (!RunTestService.testRunning) return;
        AsyncService.trading = true;
        int messageCount = getMessageCount();
        log.info("Trying to send trade tick...");
        while(messageCount > 0) {
            Thread.sleep(3000);
            messageCount = getMessageCount();
            log.info("Offers while processing: " + messageCount);
        }
        Thread.sleep(10000);
        log.info("Offers processed - sending trade tick");
        trade.acquire();
        this.rabbitTemplate.convertAndSend("trade-request-exchange", "foo.bar.#", "0");
        trade.acquire();
        trade.release();
    }

    private int getMessageCount() {
        Properties props;
        int count = 0;
        for(Queue queue : rabbitQueues) {
            props = admin.getQueueProperties(queue.getName());
            assert props != null;
            count += Integer.parseInt(props.get("QUEUE_MESSAGE_COUNT").toString());
        }
        return count;
    }

    private String generateTestName() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
