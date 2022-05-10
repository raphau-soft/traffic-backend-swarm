package com.raphau.trafficgenerator.controller;

import com.raphau.trafficgenerator.service.RunTestService;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {
    
    @Autowired
    private RunTestService runTestService;

    @Autowired
    private RabbitAdmin admin;

    @Autowired
    private List<Queue> rabbitQueues;

    @PostMapping("/run")
    public void asyncTest() throws Exception {
        runTestService.asyncTest();
    }

    @GetMapping("/stop")
    public void stopTest() throws InterruptedException {
        runTestService.stopTest();
    }

}






















