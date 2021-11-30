package com.raphau.trafficgenerator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.raphau.trafficgenerator.dao.CpuDataRepository;
import com.raphau.trafficgenerator.dao.EndpointRepository;
import com.raphau.trafficgenerator.dao.TestRepository;
import com.raphau.trafficgenerator.dto.ClientTestDTO;
import com.raphau.trafficgenerator.dto.RunTestDTO;
import com.raphau.trafficgenerator.dto.UserLogin;
import com.raphau.trafficgenerator.entity.CpuData;
import com.raphau.trafficgenerator.entity.Endpoint;
import com.raphau.trafficgenerator.entity.Test;
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

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private EndpointRepository endpointRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private CpuDataRepository cpuDataRepository;

    @PostMapping("/runTest")
    public void asyncTest(@RequestBody String name) throws Exception {

        log.info("testAsync start");
        log.info("Number of users " +  runTestDTO.getNumberOfUsers());
        if(testRepository.findAllByName(name).length != 0){
            throw new Exception();
        }

        Map<String, Integer> numberOfRequests = new HashMap<>();
        Map<String, Long> databaseTime = new HashMap<>();
        Map<String, Long> applicationTime = new HashMap<>();
        Map<String, Long> apiTime = new HashMap<>();
        List<UserLogin> userLogins = new ArrayList<>();

        asyncService.setEndWork(false);
        List<ClientTestDTO> clientTestDTOList = new ArrayList<>();

        for(int i = 0; i < runTestDTO.getNumberOfUsers(); i++){
            asyncService.postRegistration(""+i);
            userLogins.add(asyncService.login(""+i));
        }

        for(int i = 0; i < runTestDTO.getNumberOfUsers(); i++){
            Thread.sleep(1000);
            asyncService.runTests(userLogins.get(i), clientTestDTOList, runTestDTO);
        }
//        MBeanServer mbs    = ManagementFactory.getPlatformMBeanServer();
//        ObjectName objectName    = ObjectName.getInstance("java.lang:type=OperatingSystem");
        long time = runTestDTO.getTestTime();
        int x = 0;
        while(clientTestDTOList.size() < runTestDTO.getNumberOfUsers()){
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

                        CpuData cpuData = new CpuData(0, name, System.currentTimeMillis(), (Double) value);
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

        System.out.println(clientTestDTOList.size());

        for(ClientTestDTO clientTestDTO: clientTestDTOList){
            for(Map.Entry<String, Integer> entry : clientTestDTO.getNumberOfRequests().entrySet()){
                Integer number = numberOfRequests.get(entry.getKey());
                if(number == null){
                    numberOfRequests.put(entry.getKey(), entry.getValue());
                    applicationTime.put(entry.getKey(), clientTestDTO.getSummaryEndpointTime()
                            .get(entry.getKey()));
                    databaseTime.put(entry.getKey(), clientTestDTO.getSummaryEndpointDatabaseTime()
                            .get(entry.getKey()));
                    apiTime.put(entry.getKey(), clientTestDTO.getSummaryApiTime().get(entry.getKey()));
                } else {
                    numberOfRequests.put(entry.getKey(), entry.getValue() + numberOfRequests.get(entry
                            .getKey()));
                    applicationTime.put(entry.getKey(), clientTestDTO.getSummaryEndpointTime().get(entry
                            .getKey()) + applicationTime.get(entry.getKey()));
                    databaseTime.put(entry.getKey(), clientTestDTO.getSummaryEndpointDatabaseTime().get(entry
                            .getKey()) + databaseTime.get(entry.getKey()));
                    apiTime.put(entry.getKey(), clientTestDTO.getSummaryApiTime().get(entry.getKey())
                            + apiTime.get(entry.getKey()));
                }
            }
        }

        for(Map.Entry<String, Integer> entry : numberOfRequests.entrySet()){
            int numberOR = entry.getValue();
            long averageAppTime = applicationTime.get(entry.getKey()) / numberOR;
            long averageDBTime = databaseTime.get(entry.getKey()) / numberOR;
            long averageApiTime = apiTime.get(entry.getKey()) / numberOR;
            Endpoint endpoint = endpointRepository.findByEndpoint(entry.getKey()).get();
            Test test = new Test(0, endpoint, name, numberOR,
                    (int) runTestDTO.getNumberOfUsers(), averageDBTime, averageApiTime, averageAppTime);
            testRepository.save(test);
        }

    }

    @GetMapping("/getTest")
    public ResponseEntity<?> getTest(){
        List<Test> tests = testRepository.findAll();
        List<CpuData> cpuData = cpuDataRepository.findAll();
        Map<String, Object> temp = new HashMap<>();
        temp.put("tests", tests);
        temp.put("cpuData", cpuData);
        return ResponseEntity.ok(temp);
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






















