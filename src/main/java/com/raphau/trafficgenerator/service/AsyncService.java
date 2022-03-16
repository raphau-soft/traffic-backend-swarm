package com.raphau.trafficgenerator.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.raphau.trafficgenerator.dto.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.Semaphore;

import static com.raphau.trafficgenerator.configuration.UnixEpochDateTypeAdapter.getUnixEpochDateTypeAdapter;


@Service
public class AsyncService {

    private static final Logger log = LoggerFactory.getLogger(AsyncService.class);
    private final Object lock = new Object();
    private List<JSONObject> stockData;
    private List<JSONObject> usersAndCompanies;
    public static List<Semaphore> semaphores;
    private boolean endWork = false;
    public static boolean trading = false;

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
    private RabbitTemplate rabbitTemplate;

    @Async("asyncExecutor")
    public void runTests(String username, RunTestDTO runTestDTO) throws JSONException,
            InterruptedException {
        int requestsNumber = 0;
        Gson gson = new Gson();
        int user = Integer.parseInt(username);

        getStockData(user);
        semaphores.get(user).acquire();
        semaphores.get(user).release();

        JSONObject jsonObject = stockData.get(user);
        Type stockListType = new TypeToken<ArrayList<Stock>>() {}.getType();
        List<Stock> stocks = gson.fromJson(jsonObject.get("stock").toString(), stockListType);
        stockData.set(user, null);
        if (stocks.isEmpty()){
            createCompany(username);
        }
        Random randomGenerator = new Random();
        int randomStrategy = randomGenerator.nextInt() % 3;
        if (randomStrategy == 0) {
            boolean buy = true;
            for (; ; ) {
                while(trading) {
                    Thread.sleep(3000);
                }
                if (buy)
                    requestsNumber = sellAllStocks(username, requestsNumber, runTestDTO);
                else
                    requestsNumber = buyStocksUntilHaveMoney(username, requestsNumber, runTestDTO);
                buy = !buy;
                if (endWork || requestsNumber >= runTestDTO.getRequestsNumber()) {
                    synchronized (lock) {
                        RunTestService.processNumber--;
                        log.info(username + " ending, process number: " + RunTestService.processNumber);
                    }
                    return;
                }
                Thread.sleep(runTestDTO.getTimeBetweenRequests());
            }
        } else if (randomStrategy == 1) {
            boolean buy = true;
            for (; ; ) {
                while(trading) {
                    Thread.sleep(3000);
                }
                if (buy)
                    sellOneStock(username);
                else
                    buyOneStock(username);
                requestsNumber++;
                buy = !buy;
                if (endWork || requestsNumber >= runTestDTO.getRequestsNumber()) {
                    synchronized (lock) {
                        RunTestService.processNumber--;
                        log.info(username + " ending, process number: " + RunTestService.processNumber);
                    }
                    return;
                }
                Thread.sleep(runTestDTO.getTimeBetweenRequests());
            }
        } else {
            for (; ; ) {
                while(trading) {
                    Thread.sleep(3000);
                }
                int random = new Random().nextInt() % 2;
                if (random == 0) {
                    getStockData(user);
                } else {
                    getUsersAndCompanies(user);
                }
                requestsNumber++;
                if (endWork || requestsNumber >= runTestDTO.getRequestsNumber()) {
                    synchronized (lock) {
                        RunTestService.processNumber--;
                        log.info(username + " ending, process number: " + RunTestService.processNumber);
                    }
                    return;
                }
                Thread.sleep(runTestDTO.getTimeBetweenRequests());
            }
        }
    }

    private void sellOneStock(String username) throws JSONException, InterruptedException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, getUnixEpochDateTypeAdapter())
                .create();
        int user = Integer.parseInt(username);

        getStockData(user);

        semaphores.get(user).acquire();
        semaphores.get(user).release();

        JSONObject jsonObject = stockData.get(user);
        stockData.set(user, null);
        Type stockListType = new TypeToken<ArrayList<Stock>>() {}.getType();
        List<Stock> stocks = gson.fromJson(jsonObject.get("stock").toString(), stockListType);
        if (stocks.size() <= 0) return;

        int randomStock = (int) (Math.random() * 100.0 % stocks.size());
        Stock stock = stocks.get(randomStock);
        int stockAmountToSell =  (stock.getAmount() - 1) % (int)(Math.random() * 100 + 1) + 1;
        Company company = stock.getCompany();
        Type stockRateListType = new TypeToken<ArrayList<StockRate>>() {}.getType();
        List<StockRate> stockRates = gson.fromJson(jsonObject.get("stockRates")
                .toString(), stockRateListType);
        StockRate stockRateTemp = new StockRate();
        stockRateTemp.setCompany(company);
        int stockRateNum = stockRates.indexOf(stockRateTemp);
        StockRate stockRate = stockRates.get(stockRateNum);
        double rate = stockRate.getRate();
        double price = round((Math.abs(new Random().nextDouble()) % (rate * 0.3)
                + rate * 0.8), 2);
        createSellOffer(username, company.getId(), stockAmountToSell, price);
        if (endWork || trading) return;
        Thread.sleep(RunTestService.runTestDTO.getTimeBetweenRequests());
    }

    private int sellAllStocks(String username, int requestsNumber, RunTestDTO runTestDTO) throws JSONException, InterruptedException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, getUnixEpochDateTypeAdapter())
                .create();
        int user = Integer.parseInt(username);

        getStockData(user);

        semaphores.get(user).acquire();
        semaphores.get(user).release();

        JSONObject jsonObject = stockData.get(user);
        stockData.set(user, null);
        Type stockListType = new TypeToken<ArrayList<Stock>>() {}.getType();
        List<Stock> stocks = gson.fromJson(jsonObject.get("stock").toString(), stockListType);
        if (stocks.size() <= 0) return requestsNumber;
        for (Stock stock : stocks) {
            int stockAmountToSell = stock.getAmount();
            if(stockAmountToSell == 0) {
                stocks.remove(stock);
                continue;
            }
            Company company = stock.getCompany();
            Type stockRateListType = new TypeToken<ArrayList<StockRate>>() {}.getType();
            List<StockRate> stockRates = gson.fromJson(jsonObject.get("stockRates")
                    .toString(), stockRateListType);
            StockRate stockRateTemp = new StockRate();
            stockRateTemp.setCompany(company);
            int stockRateNum = stockRates.indexOf(stockRateTemp);
            StockRate stockRate = stockRates.get(stockRateNum);
            double rate = stockRate.getRate();
            double price = round((Math.abs(new Random().nextDouble()) % (rate * 0.3)
                    + rate * 0.8), 2);
            if(price == 0.0) continue;
            createSellOffer(username, company.getId(), stockAmountToSell, price);
            requestsNumber++;
            stock.setAmount(0);
            if (endWork || trading || requestsNumber >= runTestDTO.getRequestsNumber()) return requestsNumber;
            Thread.sleep(RunTestService.runTestDTO.getTimeBetweenRequests());
            if (endWork || trading || requestsNumber >= runTestDTO.getRequestsNumber()) return requestsNumber;
        }
        return requestsNumber;
    }

    private void buyOneStock(String username) throws JSONException, InterruptedException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, getUnixEpochDateTypeAdapter())
                .create();

        int userInt = Integer.parseInt(username);

        getUsersAndCompanies(userInt);

        semaphores.get(userInt).acquire();
        semaphores.get(userInt).release();

        JSONObject jsonObject = usersAndCompanies.get(userInt);
        usersAndCompanies.set(userInt, null);

        User user = gson.fromJson(jsonObject.get("user").toString(), User.class);
        double money = user.getMoney();
        Type companyListType = new TypeToken<ArrayList<Company>>() {}.getType();
        List<Company> companies = gson.fromJson(jsonObject.get("companies").toString(), companyListType);
        Type stockRateListType = new TypeToken<ArrayList<StockRate>>() {}.getType();
        List<StockRate> stockRates = gson.fromJson(jsonObject.get("stockRates").toString(), stockRateListType);
        StockRate stockRateTemp = new StockRate();
        if(companies.size() == 0) return;
        int randomCompany = (int) (Math.random() * 100.0 % companies.size());
        Company company = companies.get(randomCompany);
        stockRateTemp.setCompany(company);
        int stockRateNum = stockRates.indexOf(stockRateTemp);
        StockRate stockRate = stockRates.get(stockRateNum);
        double rate = stockRate.getRate();
        double price = round((Math.abs(new Random().nextDouble()) % (rate * 0.3) + rate * 0.9), 2);
        if(price == 0.0) return;
        int amount = (int) Math.round(Math.random() * 100.f % (money / price / 10)) + 1;
        createBuyOffer(username, company.getId(), amount, price);
        if (endWork || trading) return;
        Thread.sleep(RunTestService.runTestDTO.getTimeBetweenRequests());
    }

    private int buyStocksUntilHaveMoney(String username, int requestsNumber, RunTestDTO runTestDTO) throws JSONException, InterruptedException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, getUnixEpochDateTypeAdapter())
                .create();

        int userInt = Integer.parseInt(username);

        getUsersAndCompanies(userInt);

        semaphores.get(userInt).acquire();
        semaphores.get(userInt).release();

        JSONObject jsonObject = usersAndCompanies.get(userInt);
        usersAndCompanies.set(userInt, null);

        User user = gson.fromJson(jsonObject.get("user").toString(), User.class);
        double money = user.getMoney();
        Type companyListType = new TypeToken<ArrayList<Company>>() {}.getType();
        List<Company> companies = gson.fromJson(jsonObject.get("companies").toString(), companyListType);
        Type stockRateListType = new TypeToken<ArrayList<StockRate>>() {}.getType();
        List<StockRate> stockRates = gson.fromJson(jsonObject.get("stockRates")
                .toString(), stockRateListType);
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
                requestsNumber++;
                money -= price * amount;
                if (endWork || trading || requestsNumber >= runTestDTO.getRequestsNumber()) return requestsNumber;
                Thread.sleep(RunTestService.runTestDTO.getTimeBetweenRequests());
                if (endWork || trading || requestsNumber >= runTestDTO.getRequestsNumber()) return requestsNumber;
            }
        }
        return requestsNumber;
    }

    public void postRegistration(String username) throws InterruptedException {
        RunTestService.register.acquire();
        log.info("Register user: " + username);
        this.rabbitTemplate.convertAndSend("register-request-exchange", "foo.bar.#", username);
    }

    public void clearStockDB() throws InterruptedException {
        RunTestService.register.acquire();
        log.info("Clearing stock database");
        this.rabbitTemplate.convertAndSend("trade-request-exchange", "foo.bar.#", "1");
    }

    private void createCompany(String username) {
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

        this.rabbitTemplate.convertAndSend("company-exchange", "foo.bar.#", new CompanyDTO(0, username, name, amount, price));
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    private void createBuyOffer(String username, int companyId, int amount, double price) {
        log.info("User " + username + " sent create buy offer request");
        this.rabbitTemplate.convertAndSend("buy-offer-exchange", "foo.bar.#", new BuyOfferDTO(0, username, companyId, BigDecimal.valueOf(price), amount, new Date()));
    }

    private void createSellOffer(String username, int companyId, int amount, double price) {
        log.info("User " + username + " sent create sell offer request");
        this.rabbitTemplate.convertAndSend("sell-offer-exchange", "foo.bar.#", new SellOfferDTO(0, username, companyId, BigDecimal.valueOf(price), amount, new Date()));
    }

    private void getStockData(int user) throws InterruptedException {
        semaphores.get(user).acquire();
        log.info("User " + user + " sent stock data request");
        this.rabbitTemplate.convertAndSend("stock-data-exchange", "foo.bar.#", user);
    }

    public void getUsersAndCompanies(int user) throws InterruptedException {
        semaphores.get(user).acquire();
        log.info("User " + user + " sent user and companies data request");
        this.rabbitTemplate.convertAndSend("user-data-exchange", "foo.bar.#", user);
    }

}




















