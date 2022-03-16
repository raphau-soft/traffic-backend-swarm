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
    public static RunTestDTO runTestDTO = new RunTestDTO(1000, 50, 1800, 80);
    private final OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
    public static int processNumber = 0;
    public static Semaphore register;
    public static boolean testRunning = false;
    public static TestDTO testDTO;

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
        Test test = new Test(0, generateTestName(), System.currentTimeMillis(), null, false);
        TestParameters testParameters = new TestParameters(runTestDTO);
        testRepository.save(test);
        test.setTestParameters(testParameters);
        testParameters.setTest(test);
        testParametersRepository.save(testParameters);
        testDTO = new TestDTO(test);

        asyncService.setEndWork(false);

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

        asyncService.setStockData(objects);
        asyncService.setUsersAndCompanies(users);
        AsyncService.semaphores = semaphores;

        for(int i = 1; i <= runTestDTO.getNumberOfUsers(); i++){
            asyncService.runTests("" + i, runTestDTO);
            processNumber++;
            log.info(processNumber + "");
        }
        testRunning = true;
    }

    @Scheduled(cron = "0 */1 * * * ?")
    @Transactional
    public void checkTestStatus() {
        if(!testRunning) return;
        collectCpuData();
        long currentTime = System.currentTimeMillis();
        long testEndTime = testDTO.getStartTimestamp().getTime() + testDTO.getTestTime() * 1000L;
        if(currentTime < testEndTime) return;
        log.info("Test is going to be finished...");
        Optional<Test> testOptional = testRepository.findById(testDTO.getId());
        if(!testOptional.isPresent()) {
            testRunning = false;
            asyncService.setEndWork(true);
            return;
        }
        Test test = testOptional.get();
        test.setEndTimestamp(System.currentTimeMillis());
        test.setFinished(true);
        testRepository.save(test);
        testRunning = false;
        asyncService.setEndWork(true);
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
        log.info("Test finished");
        testRunning = false;
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void trade() throws InterruptedException {
        if (!RunTestService.testRunning) return;
        AsyncService.trading = true;
        int messageCount = getMessageCount();
        log.info("Waiting for offers to process...");
        while(messageCount > 0) {
            Thread.sleep(3000);
            messageCount = getMessageCount();
            log.info("Offers while processing: " + messageCount);
        }
        Thread.sleep(10000);
        log.info("Offers processed - sending trade tick");
        this.rabbitTemplate.convertAndSend("trade-request-exchange", "foo.bar.#", "0");
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
