package com.raphau.trafficgenerator.controller;

import com.raphau.trafficgenerator.dao.TrafficGeneratorCpuDataRepository;
import com.raphau.trafficgenerator.dao.TestParametersRepository;
import com.raphau.trafficgenerator.dao.TestRepository;
import com.raphau.trafficgenerator.dto.RunTestDTO;
import com.raphau.trafficgenerator.dto.User;
import com.raphau.trafficgenerator.dto.UserLogin;
import com.raphau.trafficgenerator.entity.TrafficGeneratorCpuData;
import com.raphau.trafficgenerator.entity.Test;
import com.raphau.trafficgenerator.entity.TestParameters;
import com.raphau.trafficgenerator.service.AsyncService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Semaphore;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {

    private static Logger log = LoggerFactory.getLogger(TestController.class);
    public static RunTestDTO runTestDTO = new RunTestDTO(1000, 50, 1, 0.5, 0.5, 0.0, 0.0, 0, 0.33, 0.33, 0.34, 1, 300000, 2000);
	private final OperatingSystemMXBean  bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    public static int processNumber = 0;
    public static Semaphore register;
    public static boolean testRunning = false;
    public static Test test;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private AsyncService asyncService;

    @Autowired
    private TestRepository testRepository;
    
    @Autowired
    private TestParametersRepository testParametersRepository;

    @Autowired
    private TrafficGeneratorCpuDataRepository cpuDataRepository;

    @PostMapping("/runTest")
    public void asyncTest() throws Exception {

        log.info("testAsync start");
        log.info("Number of users " +  runTestDTO.getNumberOfUsers());
        test = new Test(0, generateTestName(), false);
        TestParameters testParameters = new TestParameters(runTestDTO);
        testRepository.save(test);
        test.setTestParameters(testParameters);
        testParameters.setTest(test);
        testParametersRepository.save(testParameters);
        System.out.println(testParameters);

        asyncService.setEndWork(false);

        List<JSONObject> objects = Collections.synchronizedList(new ArrayList<>());
        List<JSONObject> users = Collections.synchronizedList(new ArrayList<>());
        List<Semaphore> semaphores = Collections.synchronizedList(new ArrayList<>());
        objects.add(null);
        users.add(null);
        semaphores.add(null);
        register = new Semaphore(1);
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
            Thread.sleep(1000);
            asyncService.runTests("" + i, runTestDTO);
            processNumber++;
        }
        
        long time = runTestDTO.getTestTime();
        int x = 0;
        testRunning = true;
        while(processNumber > 0){
            x--;
            if(x<=0) {

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
                        TrafficGeneratorCpuData cpuData = new TrafficGeneratorCpuData(0, test, System.currentTimeMillis(), (Double) value);
                        cpuDataRepository.save(cpuData);
                    }
                }
                x = 15;
            }
            Thread.sleep(1000);
            time -= 1000;
            log.info(time/1000 + "s left");
            if(time <= 30000){
                asyncService.setEndWork(true);
                Thread.sleep(30000);
                break;
            }
        };
        testRunning = false;
        log.info("Koniec");

    }

    @GetMapping("/stopTest")
    public void stopTest(){
        asyncService.setEndWork(true);
    }

    @GetMapping("/test")
    public ResponseEntity<?> getTest(){
    	List<Test> tests = testRepository.findAll();
    	System.out.println(tests.size());
        return ResponseEntity.ok(tests);
    }

    @PostMapping("/cleanDB")
    public void cleanDB(){
        testRepository.deleteAll();
    }

    @PostMapping("/setConf")
    public void setConf(@RequestBody RunTestDTO runTestDTO){
        this.runTestDTO = runTestDTO;
    }

    @GetMapping("/getConf")
    public ResponseEntity<?> getConf(){
        Map<String, Object> temp = new HashMap<>();
        temp.put("conf", this.runTestDTO);
        return ResponseEntity.ok(temp);
    }
    
    @GetMapping("/sendMessage")
    public void send() throws Exception{
        this.rabbitTemplate.convertAndSend("spring-boot-exchange", "foo.bar.#", "TETETETETETEST");
    }

    @GetMapping("/sendMessage2")
    public void send2() throws Exception{

    }

    private String generateTestName() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}






















