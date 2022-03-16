package com.raphau.trafficgenerator.service;

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
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

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

    @RabbitListener(queues = "test-details-response")
    @Transactional
    public void receiveOfferMessage(TrafficGeneratorTimeDataDTO trafficGeneratorTimeDataDTO) {
        if(!RunTestService.testRunning) return;
        Test test = testRepository.findById(RunTestService.testDTO.getId()).get();
        TrafficGeneratorTimeData trafficGeneratorTimeData = new TrafficGeneratorTimeData(trafficGeneratorTimeDataDTO, test);
        trafficGeneratorTimeDataRepository.save(trafficGeneratorTimeData);
    }

    @RabbitListener(queues = "cpu-data-request")
    public void receiveCpuDataMessage(CpuDataDTO cpuDataDTO) {
        if(!RunTestService.testRunning) return;
        Test test = testRepository.findById(RunTestService.testDTO.getId()).get();
        StockExchangeCpuData stockExchangeCpuData = new StockExchangeCpuData(cpuDataDTO, test);
        stockExchangeCpuDataRepository.save(stockExchangeCpuData);
        System.out.println(stockExchangeCpuData);
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
        RunTestService.registered++;
        RunTestService.register.release();
    }

    @RabbitListener(queues = "trade-response")
    public void receiveTradeMessage(TimeDataDTO timeDataDTO) {
        if(timeDataDTO.getTimestamp() != 0) {
            logger.info("Received trade time response " + timeDataDTO);
            AsyncService.trading = false;
            Test test = testRepository.findById(RunTestService.testDTO.getId()).get();
            StockExchangeTimeData stockExchangeTimeData = new StockExchangeTimeData(timeDataDTO, test);
            stockExchangeTimeDataRepository.save(stockExchangeTimeData);
        } else {
            RunTestService.register.release();
        }
    }

}
