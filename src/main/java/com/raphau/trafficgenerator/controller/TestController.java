package com.raphau.trafficgenerator.controller;

import com.raphau.trafficgenerator.service.RunTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {
    
    @Autowired
    private RunTestService runTestService;

    @PostMapping("/run")
    public void asyncTest() throws Exception {
        runTestService.asyncTest();
    }

    @GetMapping("/stop")
    public void stopTest() throws InterruptedException {
        runTestService.stopTest();
    }
}






















