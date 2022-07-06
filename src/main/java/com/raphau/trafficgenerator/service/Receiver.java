package com.raphau.trafficgenerator.service;

import com.raphau.trafficgenerator.dao.StockExchangeCpuDataRepository;
import com.raphau.trafficgenerator.dao.StockExchangeTimeDataRepository;
import com.raphau.trafficgenerator.dao.TestRepository;
import com.raphau.trafficgenerator.dao.TrafficGeneratorTimeDataRepository;
import com.raphau.trafficgenerator.dto.CpuDataDTO;
import com.raphau.trafficgenerator.dto.TimeDataDTO;
import com.raphau.trafficgenerator.dto.TrafficGeneratorTimeDataDTO;
import com.raphau.trafficgenerator.entity.StockExchangeCpuData;
import com.raphau.trafficgenerator.entity.StockExchangeTimeData;
import com.raphau.trafficgenerator.entity.Test;
import com.raphau.trafficgenerator.entity.TrafficGeneratorTimeData;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
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
        try {
            Test test = testRepository.findById(RunTestService.testDTO.getId()).get();
            TrafficGeneratorTimeData trafficGeneratorTimeData = trafficGeneratorTimeDataRepository.getOne(trafficGeneratorTimeDataDTO.getId());
            trafficGeneratorTimeData.updateWithDTO(trafficGeneratorTimeData, trafficGeneratorTimeDataDTO, test);
            trafficGeneratorTimeDataRepository.save(trafficGeneratorTimeData);
        } catch (NullPointerException ignored) {

        }
    }

    @RabbitListener(queues = "cpu-data-request")
    public void receiveCpuDataMessage(CpuDataDTO cpuDataDTO) {
        if (!RunTestService.testRunning) return;
        try {
            Test test = testRepository.findById(RunTestService.testDTO.getId()).get();
            StockExchangeCpuData stockExchangeCpuData = new StockExchangeCpuData(cpuDataDTO, test);
            stockExchangeCpuDataRepository.save(stockExchangeCpuData);
        } catch (NullPointerException ignored) {

        }
    }

    @RabbitListener(queues = "stock-data-response")
    public void receiveStockDataMessage(Map<String, Object> objects) {
        int user = Integer.parseInt((String) objects.get("username"));
        try {
            asyncService.getStockData().set(user, new JSONObject(objects));
            AsyncService.semaphores.get(user).release();
        } catch (NullPointerException ignored) {
            AsyncService.semaphores.get(user).release();
        }
    }

    @RabbitListener(queues = "user-data-response")
    public void receiveUserDataMessage(Map<String, Object> objects) {
        int user = Integer.parseInt((String) objects.get("username"));
        try {
            asyncService.getUsersAndCompanies().set(user, new JSONObject(objects));
            AsyncService.semaphores.get(user).release();
        } catch (NullPointerException ignored) {
            AsyncService.semaphores.get(user).release();
        }
    }

    @RabbitListener(queues = "register-response")
    public void receiveRegisterMessage(String flag) {
        if (flag.equals("0")) {
            RunTestService.registered++;
        }
        if (flag.equals("1")) {
            RunTestService.validator++;
        }
    }

    @RabbitListener(queues = "trade-response")
    public void receiveTradeMessage(TimeDataDTO timeDataDTO) {
        logger.info("Trade response");
        if (timeDataDTO.getTimestamp() != 0) {
            logger.info("Received trade time response " + timeDataDTO);
            AsyncService.trading = false;
            Test test = testRepository.findById(RunTestService.testDTO.getId()).get();
            StockExchangeTimeData stockExchangeTimeData = new StockExchangeTimeData(timeDataDTO, test);
            stockExchangeTimeDataRepository.save(stockExchangeTimeData);
        } else {
            logger.info("Trade response: " + timeDataDTO);
            RunTestService.register.release();
        }
    }

}
