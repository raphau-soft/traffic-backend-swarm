package com.raphau.trafficgenerator.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.raphau.trafficgenerator.dao.TestRepository;
import com.raphau.trafficgenerator.dao.TrafficGeneratorTimeDataRepository;
import com.raphau.trafficgenerator.dto.*;
import com.raphau.trafficgenerator.entity.Test;
import com.raphau.trafficgenerator.entity.TrafficGeneratorTimeData;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpResourceNotAvailableException;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import static com.raphau.trafficgenerator.configuration.UnixEpochDateTypeAdapter.getUnixEpochDateTypeAdapter;


@Service
public class AsyncService {

    private static final Logger log = LoggerFactory.getLogger(AsyncService.class);
    public final Object lock = new Object();
    private List<JSONObject> stockData;
    private List<JSONObject> usersAndCompanies;
    public static List<Semaphore> semaphores;
    private boolean endWork = false;
    public static boolean trading = false;

    public boolean isEndWork() {
        return endWork;
    }

    public List<JSONObject> getUsersAndCompanies() {
        return usersAndCompanies;
    }

    public void setUsersAndCompanies(List<JSONObject> usersAndCompanies) {
        this.usersAndCompanies = usersAndCompanies;
    }

    public void setEndWork(boolean endWork) {
        this.endWork = endWork;
    }

    public void setStockData(List<JSONObject> stockData) {
        this.stockData = stockData;
    }

    public List<JSONObject> getStockData() {
        return stockData;
    }

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private TrafficGeneratorTimeDataRepository trafficGeneratorTimeDataRepository;

    @Autowired
    private RabbitAdmin admin;

    @Async("asyncExecutor")
    public void runTests(String username, RunTestDTO runTestDTO, int strategy) {
        int requestsNumber = 0;
        Gson gson = new Gson();
        int user = Integer.parseInt(username);

        try {
            getStockData(user);
            semaphores.get(user).acquire();
        } catch (InterruptedException e) {
            RunTestService.processNumber--;
            log.info(username + " ending, @@@ process number: " + RunTestService.processNumber);
            return;
        }
        semaphores.get(user).release();

        JSONObject jsonObject = stockData.get(user);
        Type stockListType = new TypeToken<ArrayList<Stock>>() {
        }.getType();
        List<Stock> stocks;
        try {
            stocks = gson.fromJson(jsonObject.get("stock").toString(), stockListType);
        } catch (JSONException e) {
            RunTestService.processNumber--;
            log.info(username + " ending, @@@ process number: " + RunTestService.processNumber);
            return;
        }
        stockData.set(user, null);
        if (stocks != null && stocks.isEmpty()) {
            createCompany(username);
        }
        log.debug("User: " + username + ", strategy: " + strategy);
        if (strategy == 1) {
            boolean buy = true;
            for (; ; ) {
                while (trading) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        RunTestService.processNumber--;
                        log.info(username + " ending, @@@ process number: " + RunTestService.processNumber);
                        return;
                    }
                }
                if (buy)
                    requestsNumber = sellAllStocks(username, requestsNumber, runTestDTO);
                else
                    requestsNumber = buyStocksUntilHaveMoney(username, requestsNumber, runTestDTO);
                buy = !buy;
                if (runTestDTO.isRequestLimit() && requestsNumber >= runTestDTO.getRequestsNumber() || endWork) {
                    synchronized (lock) {
                        RunTestService.processNumber--;
                        log.info(username + " ending, @@@ process number: " + RunTestService.processNumber);
                    }
                    return;
                }
                try {
                    Thread.sleep(runTestDTO.getTimeBetweenRequests());
                } catch (InterruptedException e) {
                    RunTestService.processNumber--;
                    log.info(username + " ending, @@@ process number: " + RunTestService.processNumber);
                    return;
                }
            }
        } else if (strategy == 2) {
            boolean buy = true;
            for (; ; ) {
                while (trading) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        RunTestService.processNumber--;
                        log.info(username + " ending, @@@ process number: " + RunTestService.processNumber);
                        return;
                    }
                }
                if (buy)
                    sellOneStock(username);
                else
                    buyOneStock(username);
                if (runTestDTO.isRequestLimit())
                    requestsNumber++;
                buy = !buy;
                if (runTestDTO.isRequestLimit() && requestsNumber >= runTestDTO.getRequestsNumber() || endWork) {
                    synchronized (lock) {
                        RunTestService.processNumber--;
                        log.info(username + " ending, @@@ process number: " + RunTestService.processNumber);
                    }
                    return;
                }
                try {
                    Thread.sleep(runTestDTO.getTimeBetweenRequests());
                } catch (InterruptedException e) {
                    RunTestService.processNumber--;
                    log.info(username + " ending, @@@ process number: " + RunTestService.processNumber);
                    return;
                }
            }
        } else {
            for (; ; ) {
                while (trading) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        RunTestService.processNumber--;
                        log.info(username + " ending, @@@ process number: " + RunTestService.processNumber);
                        return;
                    }
                }
                int random = new Random().nextInt() % 2;
                if (random == 0) {
                    try {
                        getStockData(user);
                    } catch (InterruptedException e) {
                        RunTestService.processNumber--;
                        log.info(username + " ending, @@@ process number: " + RunTestService.processNumber);
                        return;
                    }
                } else {
                    try {
                        getUsersAndCompanies(user);
                    } catch (InterruptedException e) {
                        RunTestService.processNumber--;
                        log.info(username + " ending, @@@ process number: " + RunTestService.processNumber);
                        return;
                    }
                }
                requestsNumber++;
                if (runTestDTO.isRequestLimit() && requestsNumber >= runTestDTO.getRequestsNumber() || endWork) {
                    synchronized (lock) {
                        RunTestService.processNumber--;
                        log.info(username + " ending, @@@ process number: " + RunTestService.processNumber);
                    }
                    return;
                }
                try {
                    Thread.sleep(runTestDTO.getTimeBetweenRequests());
                } catch (InterruptedException e) {
                    RunTestService.processNumber--;
                    log.info(username + " ending, @@@ process number: " + RunTestService.processNumber);
                    return;
                }
            }
        }
    }

    private void sellOneStock(String username) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, getUnixEpochDateTypeAdapter())
                .create();
        int user = Integer.parseInt(username);

        try {
            getStockData(user);
            semaphores.get(user).acquire();
        } catch (InterruptedException e) {
            return;
        }

        semaphores.get(user).release();

        JSONObject jsonObject = stockData.get(user);
        stockData.set(user, null);
        Type stockListType = new TypeToken<ArrayList<Stock>>() {
        }.getType();
        List<Stock> stocks;
        try {
            stocks = gson.fromJson(jsonObject.get("stock").toString(), stockListType);
        } catch (JSONException e) {
            return;
        }
        if (stocks.size() <= 0) return;

        int randomStock = (int) (Math.random() * 100.0 % stocks.size());
        Stock stock = stocks.get(randomStock);
        int stockAmountToSell = (stock.getAmount() - 1) % (int) (Math.random() * 100 + 1) + 1;
        Company company = stock.getCompany();
        Type stockRateListType = new TypeToken<ArrayList<StockRate>>() {
        }.getType();
        List<StockRate> stockRates;
        try {
            stockRates = gson.fromJson(jsonObject.get("stockRates")
                    .toString(), stockRateListType);
        } catch (JSONException e) {
            return;
        }
        StockRate stockRateTemp = new StockRate();
        stockRateTemp.setCompany(company);
        int stockRateNum = stockRates.indexOf(stockRateTemp);
        StockRate stockRate = stockRates.get(stockRateNum);
        double rate = stockRate.getRate();
        double price = round((Math.abs(new Random().nextDouble()) % (rate * 0.3)
                + rate * 0.8), 2);
        createSellOffer(username, company.getId(), stockAmountToSell, price);
        if (endWork || trading) return;
        try {
            Thread.sleep(RunTestService.runTestDTO.getTimeBetweenRequests());
        } catch (InterruptedException ignored) {
        }
    }

    private int sellAllStocks(String username, int requestsNumber, RunTestDTO runTestDTO) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, getUnixEpochDateTypeAdapter())
                .create();
        int user = Integer.parseInt(username);

        try {
            getStockData(user);
            semaphores.get(user).acquire();
        } catch (InterruptedException e) {
            return 1;
        }

        semaphores.get(user).release();

        JSONObject jsonObject = stockData.get(user);
        stockData.set(user, null);
        Type stockListType = new TypeToken<ArrayList<Stock>>() {
        }.getType();

        List<Stock> stocks;
        try {
            stocks = gson.fromJson(jsonObject.get("stock").toString(), stockListType);
        } catch (JSONException e) {
            return 1;
        }

        if (stocks.size() <= 0) return requestsNumber;
        for (Stock stock : stocks) {
            int stockAmountToSell = stock.getAmount();
            if (stockAmountToSell == 0) {
                stocks.remove(stock);
                continue;
            }
            Company company = stock.getCompany();
            Type stockRateListType = new TypeToken<ArrayList<StockRate>>() {
            }.getType();
            List<StockRate> stockRates;
            try {
                stockRates = gson.fromJson(jsonObject.get("stockRates").toString(), stockRateListType);
            } catch (JSONException e) {
                return 1;
            }
            StockRate stockRateTemp = new StockRate();
            stockRateTemp.setCompany(company);
            int stockRateNum = stockRates.indexOf(stockRateTemp);
            StockRate stockRate = stockRates.get(stockRateNum);
            double rate = stockRate.getRate();
            double price = round((Math.abs(new Random().nextDouble()) % (rate * 0.3)
                    + rate * 0.8), 2);
            if (price == 0.0) continue;
            createSellOffer(username, company.getId(), stockAmountToSell, price);
            if (runTestDTO.isRequestLimit()) {
                requestsNumber++;
                if (username.equals("1")) {
                    log.info("Number of requests: " + requestsNumber);
                }
            }
            stock.setAmount(0);
            if (runTestDTO.isRequestLimit() && requestsNumber >= runTestDTO.getRequestsNumber() || endWork || trading)
                return requestsNumber;
            try {
                Thread.sleep(RunTestService.runTestDTO.getTimeBetweenRequests());
            } catch (InterruptedException e) {
                return 1;
            }
            if (runTestDTO.isRequestLimit() && requestsNumber >= runTestDTO.getRequestsNumber() || endWork || trading)
                return requestsNumber;
        }
        return requestsNumber;
    }

    private void buyOneStock(String username) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, getUnixEpochDateTypeAdapter())
                .create();

        int userInt = Integer.parseInt(username);

        try {
            getUsersAndCompanies(userInt);
            semaphores.get(userInt).acquire();
        } catch (InterruptedException e) {
            return;
        }

        semaphores.get(userInt).release();

        JSONObject jsonObject = usersAndCompanies.get(userInt);
        usersAndCompanies.set(userInt, null);
        User user;
        try {
            user = gson.fromJson(jsonObject.get("user").toString(), User.class);
        } catch (JSONException e) {
            return;
        }
        double money = user.getMoney();
        Type companyListType = new TypeToken<ArrayList<Company>>() {
        }.getType();
        List<Company> companies;
        try {
            companies = gson.fromJson(jsonObject.get("companies").toString(), companyListType);
        } catch (JSONException e) {
            return;
        }
        Type stockRateListType = new TypeToken<ArrayList<StockRate>>() {
        }.getType();
        List<StockRate> stockRates;
        try {
            stockRates = gson.fromJson(jsonObject.get("stockRates").toString(), stockRateListType);
        } catch (JSONException e) {
            return;
        }
        StockRate stockRateTemp = new StockRate();
        if (companies.size() == 0) return;
        int randomCompany = (int) (Math.random() * 100.0 % companies.size());
        Company company = companies.get(randomCompany);
        stockRateTemp.setCompany(company);
        int stockRateNum = stockRates.indexOf(stockRateTemp);
        StockRate stockRate = stockRates.get(stockRateNum);
        double rate = stockRate.getRate();
        double price = round((Math.abs(new Random().nextDouble()) % (rate * 0.3) + rate * 0.9), 2);
        if (price == 0.0) return;
        int amount = (int) Math.round(Math.random() * 100.f % (money / price / 10)) + 1;
        createBuyOffer(username, company.getId(), amount, price);
        if (endWork || trading) return;
        try {
            Thread.sleep(RunTestService.runTestDTO.getTimeBetweenRequests());
        } catch (InterruptedException ignored) {

        }
    }

    private int buyStocksUntilHaveMoney(String username, int requestsNumber, RunTestDTO runTestDTO) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, getUnixEpochDateTypeAdapter())
                .create();

        int userInt = Integer.parseInt(username);
        try {
            getUsersAndCompanies(userInt);
            semaphores.get(userInt).acquire();
        } catch (InterruptedException e) {
            return 1;
        }

        semaphores.get(userInt).release();

        JSONObject jsonObject = usersAndCompanies.get(userInt);
        usersAndCompanies.set(userInt, null);

        User user;
        try {
            user = gson.fromJson(jsonObject.get("user").toString(), User.class);
        } catch (JSONException e) {
            return 1;
        }
        double money = user.getMoney();
        Type companyListType = new TypeToken<ArrayList<Company>>() {
        }.getType();
        List<Company> companies;
        try {
            companies = gson.fromJson(jsonObject.get("companies").toString(), companyListType);
        } catch (JSONException e) {
            return 1;
        }
        Type stockRateListType = new TypeToken<ArrayList<StockRate>>() {
        }.getType();
        List<StockRate> stockRates;
        try {
            stockRates = gson.fromJson(jsonObject.get("stockRates")
                    .toString(), stockRateListType);
        } catch (JSONException e) {
            return 1;
        }
        StockRate stockRateTemp = new StockRate();
        mainLoop:
        for (; ; ) {
            for (Company company : companies) {
                stockRateTemp.setCompany(company);
                int stockRateNum = stockRates.indexOf(stockRateTemp);
                StockRate stockRate = stockRates.get(stockRateNum);
                double rate = stockRate.getRate();
                double price = round((Math.abs(new Random().nextDouble()) % (rate * 0.3) + rate * 0.9), 2);
                int maxAmount = (int) (money / price);
                int amount = (int) Math.round(Math.random() * 100.f % maxAmount);
                if (amount == 0) break mainLoop;
                createBuyOffer(username, company.getId(), amount, price);
                if (runTestDTO.isRequestLimit()) {
                    requestsNumber++;
                    if (username.equals("1")) {
                        log.info("Number of requests: " + requestsNumber);
                    }
                }
                money -= price * amount;
                if (runTestDTO.isRequestLimit() && requestsNumber >= runTestDTO.getRequestsNumber() || endWork || trading)
                    return requestsNumber;
                try {
                    Thread.sleep(RunTestService.runTestDTO.getTimeBetweenRequests());
                } catch (InterruptedException e) {
                    return 1;
                }
                if (runTestDTO.isRequestLimit() && requestsNumber >= runTestDTO.getRequestsNumber() || endWork || trading)
                    return requestsNumber;
            }
        }
        return requestsNumber;
    }

    public void postRegistration(String username) throws InterruptedException {
        Test test = testRepository.findById(RunTestService.testDTO.getId()).get();
        QueueInformation information = admin.getQueueInfo("register-request");
        assert information != null;
        int unack = getUnack("register-request");
        TrafficGeneratorTimeData trafficGeneratorTimeData = new TrafficGeneratorTimeData(0, test, System.currentTimeMillis(), null, null, System.currentTimeMillis(), information.getMessageCount() + unack, "do-register");
        trafficGeneratorTimeDataRepository.saveAndFlush(trafficGeneratorTimeData);
        log.debug("Register user: " + username);
        this.rabbitTemplate.convertAndSend("register-request-exchange", "foo.bar.#", new GetDataDTO(username, trafficGeneratorTimeData.getId()));
    }

    public void clearStockDB() throws InterruptedException {
        RunTestService.register.acquire();
        log.info("Clearing stock database");
        this.rabbitTemplate.convertAndSend("trade-request-exchange", "foo.bar.#", "1");
        RunTestService.register.acquire();
    }

    public void sendFinishTrading() {
        log.info("Sending finish trading tick");
        for (int i = 0; i < 10; i++)
            this.rabbitTemplate.convertAndSend("trade-request-exchange", "foo.bar.#", "2");
    }

    void createCompany(String username) {
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 7;
        Random random = new Random();
        StringBuilder sb = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int) (random.nextDouble() * (rightLimit - leftLimit + 1));
            sb.append((char) randomLimitedInt);
        }
        String name = sb.toString();
        int amount = Math.abs(new Random().nextInt() % 1500) + 50;
        double price = Math.round(new Random().nextDouble() * 10000) / 100.0;
        Test test = testRepository.findById(RunTestService.testDTO.getId()).get();
        QueueInformation information = admin.getQueueInfo("company-request");
        assert information != null;
        int unack = getUnack("company-request");
        TrafficGeneratorTimeData trafficGeneratorTimeData = new TrafficGeneratorTimeData(0, test, System.currentTimeMillis(), null, null, System.currentTimeMillis(), information.getMessageCount() + unack, "add-company");
        trafficGeneratorTimeDataRepository.saveAndFlush(trafficGeneratorTimeData);
        this.rabbitTemplate.convertAndSend("company-exchange", "foo.bar.#", new CompanyDTO(0, username, name, amount, price, trafficGeneratorTimeData.getId()));
    }

    private int getUnack(String queue) {
//        log.info(url);
//
//        ResponseEntity<String> response
//                = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(createHeaders()), String.class);
//        Matcher matcher = Pattern.compile("(\"messages_unacknowledged\": )([0-9]*)(,)(.*)(\"name\": \")" + queue).matcher(response.getBody());
//
//        if(matcher.find()) {
//            String value = matcher.group(2);
//            log.info(value);
//            return  Integer.parseInt(value);
//        }
        return 0;
    }

    HttpHeaders createHeaders() {
        return new HttpHeaders() {{
            String authHeader = "Basic Z3Vlc3Q6Z3Vlc3Q=";
            set("Authorization", authHeader);
        }};
    }

    public static double round(double value, int places) {
        if (places < 0) places = 0;
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    void createBuyOffer(String username, int companyId, int amount, double price) {
        log.info("User " + username + " sent create buy offer request");
        Test test = testRepository.findById(RunTestService.testDTO.getId()).get();
        QueueInformation information = admin.getQueueInfo("buy-offer-request");
        assert information != null;
        int unack = getUnack("buy-offer-request");
        TrafficGeneratorTimeData trafficGeneratorTimeData = new TrafficGeneratorTimeData(0, test, System.currentTimeMillis(), null, null, System.currentTimeMillis(), information.getMessageCount() + unack, "add-buy-offer");
        try {
            trafficGeneratorTimeDataRepository.saveAndFlush(trafficGeneratorTimeData);
            this.rabbitTemplate.convertAndSend("buy-offer-exchange", "foo.bar.#", new BuyOfferDTO(0, username, companyId, BigDecimal.valueOf(price), amount, new Date(), trafficGeneratorTimeData.getId()));
        } catch (AmqpResourceNotAvailableException e) {
            log.info("User " + username + " channel is full");
        }
    }

    void createSellOffer(String username, int companyId, int amount, double price) {
        log.debug("User " + username + " sent create sell offer request");
        Test test = testRepository.findById(RunTestService.testDTO.getId()).get();
        QueueInformation information = admin.getQueueInfo("sell-offer-request");
        assert information != null;
        int unack = getUnack("sell-offer-request");
        TrafficGeneratorTimeData trafficGeneratorTimeData = new TrafficGeneratorTimeData(0, test, System.currentTimeMillis(), null, null, System.currentTimeMillis(), information.getMessageCount() + unack, "add-sell-offer");
        try {
            trafficGeneratorTimeDataRepository.saveAndFlush(trafficGeneratorTimeData);
            this.rabbitTemplate.convertAndSend("sell-offer-exchange", "foo.bar.#", new SellOfferDTO(0, username, companyId, BigDecimal.valueOf(price), amount, new Date(), trafficGeneratorTimeData.getId()));
        } catch (AmqpResourceNotAvailableException e) {
            log.info("User " + username + " channel is full");
        }
    }

    private void getStockData(int user) throws InterruptedException {
        semaphores.get(user).acquire();
        Test test = testRepository.findById(RunTestService.testDTO.getId()).get();
        log.debug("User " + user + " sent stock data request");
        QueueInformation information = admin.getQueueInfo("stock-data-request");
        assert information != null;
        int unack = getUnack("stock-data-request");
        TrafficGeneratorTimeData trafficGeneratorTimeData = new TrafficGeneratorTimeData(0, test, System.currentTimeMillis(), null, null, System.currentTimeMillis(), information.getMessageCount() + unack, "get-stock-data");
        trafficGeneratorTimeDataRepository.saveAndFlush(trafficGeneratorTimeData);
        try {
            this.rabbitTemplate.convertAndSend("stock-data-exchange", "foo.bar.#", new GetDataDTO(String.valueOf(user), trafficGeneratorTimeData.getId()));
        } catch (AmqpResourceNotAvailableException e) {
            log.debug("User " + user + " channel is full");
            semaphores.get(user).release();
        }
    }

    public void getUsersAndCompanies(int user) throws InterruptedException {
        semaphores.get(user).acquire();
        Test test = testRepository.findById(RunTestService.testDTO.getId()).get();
        log.debug("User " + user + " sent user and companies data request");
        QueueInformation information = admin.getQueueInfo("user-data-request");
        assert information != null;
        int unack = getUnack("user-data-request");
        TrafficGeneratorTimeData trafficGeneratorTimeData = new TrafficGeneratorTimeData(0, test, System.currentTimeMillis(), null, null, System.currentTimeMillis(), information.getMessageCount() + unack, "get-stock-users-and-companies");
        trafficGeneratorTimeDataRepository.saveAndFlush(trafficGeneratorTimeData);
        try {
            this.rabbitTemplate.convertAndSend("user-data-exchange", "foo.bar.#", new GetDataDTO(String.valueOf(user), trafficGeneratorTimeData.getId()));
        } catch (AmqpResourceNotAvailableException e) {
            log.debug("User " + user + " channel is full");
            semaphores.get(user).release();
        }
    }

}




















