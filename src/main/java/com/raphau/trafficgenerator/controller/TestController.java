package com.raphau.trafficgenerator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.raphau.trafficgenerator.dao.CpuDataRepository;
import com.raphau.trafficgenerator.dao.TestParametersRepository;
import com.raphau.trafficgenerator.dao.TestRepository;
import com.raphau.trafficgenerator.dto.ClientTestDTO;
import com.raphau.trafficgenerator.dto.RunTestDTO;
import com.raphau.trafficgenerator.dto.UserLogin;
import com.raphau.trafficgenerator.entity.CpuData;
import com.raphau.trafficgenerator.entity.Test;
import com.raphau.trafficgenerator.entity.TestParameters;
import com.raphau.trafficgenerator.service.AsyncService;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {

    private static Logger log = LoggerFactory.getLogger(TestController.class);
    private RunTestDTO runTestDTO = new RunTestDTO(20, 50, 0.9, 0.45, 0.45, 0.05, 0.05, 0.1, 0.33, 0.33, 0.34, 1, 120000, 20);
    private final OperatingSystemMXBean  bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    public static int processNumber = 0;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private TestRepository testRepository;
    
    @Autowired
    private TestParametersRepository testParametersRepository;

    @Autowired
    private CpuDataRepository cpuDataRepository;

    @PostMapping("/runTest")
    public void asyncTest(@RequestBody String name) throws Exception {

        log.info("testAsync start");
        log.info("Number of users " +  runTestDTO.getNumberOfUsers());
        if(testRepository.findAllByName(name).length != 0){
            throw new Exception();
        }
        
        Test test = new Test(0, name, false);
        TestParameters testParameters = new TestParameters(runTestDTO);
        testParametersRepository.save(testParameters);
        test.setTestParameters(testParameters);
        testRepository.save(test);

        List<UserLogin> userLogins = new ArrayList<>();
        asyncService.setEndWork(false);

        for(int i = 0; i < runTestDTO.getNumberOfUsers(); i++){
            asyncService.postRegistration(""+i);
            userLogins.add(asyncService.login(""+i));
        }

        for(int i = 0; i < runTestDTO.getNumberOfUsers(); i++){
            Thread.sleep(1000);
            asyncService.runTests(userLogins.get(i), runTestDTO);
            processNumber++;
        }
        
        long time = runTestDTO.getTestTime();
        int x = 0;
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
                        CpuData cpuData = new CpuData(0, test, System.currentTimeMillis(), (Double) value);
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
        log.info("Koniec");

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

}






















