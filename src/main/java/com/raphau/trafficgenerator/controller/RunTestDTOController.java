package com.raphau.trafficgenerator.controller;

import com.raphau.trafficgenerator.configuration.SchemaHandler;
import com.raphau.trafficgenerator.dao.*;
import com.raphau.trafficgenerator.dto.RunTestDTO;
import com.raphau.trafficgenerator.dto.TestDTO;
import com.raphau.trafficgenerator.entity.*;
import com.raphau.trafficgenerator.service.RunTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class RunTestDTOController {

    private static final Logger log = LoggerFactory.getLogger(RunTestDTOController.class);

    @Autowired
    private RunTestService runTestService;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private StockExchangeTimeDataRepository stockExchangeTimeDataRepository;

    @Autowired
    private StockExchangeCpuDataRepository stockExchangeCpuDataRepository;

    @Autowired
    private TrafficGeneratorCpuDataRepository trafficGeneratorCpuDataRepository;

    @Autowired
    private TrafficGeneratorTimeDataRepository trafficGeneratorTimeDataRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SchemaHandler schemaHandler;

    @GetMapping("/index")
    public String index(Model model) {
        List<Test> tests = testRepository.findAll();
        List<TestDTO> testDTOS = tests.stream().map(TestDTO::new).collect(Collectors.toList());

        model.addAttribute("runTestDTO", RunTestService.runTestDTO);
        model.addAttribute("tests", testDTOS);
        model.addAttribute("testRunning", RunTestService.testRunning);
        return "index";
    }

    @GetMapping("/cleanTrafficDB")
    public String cleanTrafficDB() throws Exception {
        schemaHandler.execute();
        return "redirect:/index";
    }

    @GetMapping("/cleanStockDB")
    public String cleanStockDB() {
        clearStockDB();
        return "redirect:/index";
    }

    public void clearStockDB() {
        log.info("Clearing stock database");
        this.rabbitTemplate.convertAndSend("trade-request-exchange", "foo.bar.#", "1");
    }

    @GetMapping("/setconf")
    public String setConf(Model model) {
        model.addAttribute("runTestDTO", RunTestService.runTestDTO);
        return "setconf";
    }

    @PostMapping("/postconf")
    public String postConf(@Valid RunTestDTO runTestDTO, BindingResult bindingResult) {
        if(!runTestDTO.isRequestLimit() && !runTestDTO.isTimeLimit()) {
            bindingResult.addError(new FieldError("runTestDTO", "timeLimit", "At least one limiter is required"));
        }
        if(bindingResult.hasErrors()){
            return "setconf";
        }
        RunTestService.runTestDTO = runTestDTO;
        log.info("Test configuration set to: " + runTestDTO);
        return "redirect:/index";
    }

    @GetMapping("/runTest")
    public String runTest() throws Exception {
        runTestService.asyncTest();
        return "redirect:/index";
    }

    @GetMapping("/stopTest")
    public String stopTest() throws InterruptedException {
        runTestService.stopTest();
        return "redirect:/index";
    }

    @GetMapping("/test")
    public String test(@RequestParam int id, Model model) throws Exception {
        Optional<Test> testOptional = testRepository.findById(id);
        if(!testOptional.isPresent()) return "redirect:/index";
        model.addAttribute("test", new TestDTO(testOptional.get()));
        model.addAttribute("testParameters", testOptional.get().getTestParameters());
        return "test";
    }

    @GetMapping("/data")
    public void data(@RequestParam int p, @RequestParam int id, HttpServletResponse response) throws Exception {
        Optional<Test> testOptional = testRepository.findById(id);
        if(!testOptional.isPresent()) return;
        Test test = testOptional.get();
        response.setContentType("text/csv");
        String headerKey = "Content-Disposition";
        String headerValue;
        switch(p) {
            case 1:
                headerValue = "attachment; filename=methodsTimeData" + test.getName() + ".csv";
                response.setHeader(headerKey, headerValue);
                getMethodsTimeData(response, test);
                break;
            case 2:
                headerValue = "attachment; filename=tradingTimeData_" + test.getName() + ".csv";
                response.setHeader(headerKey, headerValue);
                getTradingTimeData(response, test);
                break;
            case 3:
                headerValue = "attachment; filename=stockCpuData_" + test.getName() + ".csv";
                response.setHeader(headerKey, headerValue);
                getStockCpuData(response, test);
                break;
            case 4:
                headerValue = "attachment; filename=trafficCpuData_" + test.getName() + ".csv";
                response.setHeader(headerKey, headerValue);
                getTrafficCpuData(response, test);
                break;
        }
    }

    private void getMethodsTimeData(HttpServletResponse response, Test test) throws IOException {
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        String[] csvHeader = {"timestamp", "apiTime", "applicationTime", "databaseTime",  "endpointUrl", "queueSizeForward", "queueSizeBack", "replicaId"};
        String[] nameMapping = {"timestamp", "apiTime", "applicationTime", "databaseTime",  "endpointUrl", "queueSizeForward", "queueSizeBack", "stockId"};
        csvWriter.writeHeader(csvHeader);
        List<TrafficGeneratorTimeData> list;
        int pageSize = 10000;
        int currentPage = 0;
        do {
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<TrafficGeneratorTimeData> page = trafficGeneratorTimeDataRepository.findAllByTest(pageable, test);
            list = page.getContent();
            for (TrafficGeneratorTimeData data : list) {
                csvWriter.write(data, nameMapping);
            }
            currentPage++;
        } while (list.size() == 10000);
        csvWriter.close();
    }

    private void getTradingTimeData(HttpServletResponse response, Test test) throws IOException {
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        String[] csvHeader = {"timestamp", "applicationTime", "databaseTime", "numberOfSellOffers", "numberOfBuyOffers", "replicaId"};
        String[] nameMapping = {"timestamp", "applicationTime", "databaseTime", "numberOfSellOffers", "numberOfBuyOffers", "stockId"};
        csvWriter.writeHeader(csvHeader);
        List<StockExchangeTimeData> list;
        int pageSize = 10000;
        int currentPage = 0;
        do {
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<StockExchangeTimeData> page = stockExchangeTimeDataRepository.findAllByTest(pageable, test);
            list = page.getContent();
            for (StockExchangeTimeData data : list) {
                csvWriter.write(data, nameMapping);
            }
            currentPage++;
        } while (list.size() == 10000);
        csvWriter.close();
    }

    private void getStockCpuData(HttpServletResponse response, Test test) throws IOException {
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        String[] csvHeader = {"timestamp", "cpuUsage", "memoryUsage", "replicaId"};
        String[] nameMapping = {"timestamp", "cpuUsage", "memory", "stockId"};
        csvWriter.writeHeader(csvHeader);
        List<StockExchangeCpuData> list;
        int pageSize = 10000;
        int currentPage = 0;
        do {
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<StockExchangeCpuData> page = stockExchangeCpuDataRepository.findAllByTest(pageable, test);
            list = page.getContent();
            for (StockExchangeCpuData data : list) {
                csvWriter.write(data, nameMapping);
            }
            currentPage++;
        } while (list.size() == 10000);

        csvWriter.close();
    }

    private void getTrafficCpuData(HttpServletResponse response, Test test) throws IOException {
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        String[] csvHeader = {"timestamp", "cpuUsage", "memoryUsage"};
        String[] nameMapping = {"timestamp", "cpuUsage", "memory"};
        csvWriter.writeHeader(csvHeader);
        List<TrafficGeneratorCpuData> list;
        int pageSize = 10000;
        int currentPage = 0;
        do {
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<TrafficGeneratorCpuData> page = trafficGeneratorCpuDataRepository.findAllByTest(pageable, test);
            list = page.getContent();
            for (TrafficGeneratorCpuData data : list) {
                csvWriter.write(data, nameMapping);
            }
            currentPage++;
        } while (list.size() == 10000);
        csvWriter.close();
    }
}
