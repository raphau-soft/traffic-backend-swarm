package com.raphau.trafficgenerator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.raphau.trafficgenerator.controller.TestController;
import com.raphau.trafficgenerator.dto.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


@Service
public class AsyncService {

    private static Logger log = LoggerFactory.getLogger(AsyncService.class);
    private HttpHeaders headers;
    private final Object lock = new Object();

    // playing on stock strategy
    private final int RAND_EXPENSIVE_ONE_COMP = 0;
    private final int RAND_RANDOM_MANY_COMP = 1;
    private final int RAND_CHEAP_MANY_COMP = 2;
    private final String SIGNIN = "signin";
    private final String SIGNUP = "signup";
    private final String BUYOFFER = "buyOffer";
    private final String COMPANIES = "companies";
    private final String COMPANY = "company";
    private final String SELLOFFER = "sellOffer";
    private final String STOCKRATES = "stockRates";
    // private final String TRANSACTIONS = "transactions";
    private final String USER_RESOURCES = "user/resources";
    private final String USER = "user";
    private final String USER_BUYOFFERS = "user/buyOffers";
    private final String USER_BUYOFFERS_D = "user/buyOffers/id";
    private final String USER_SELLOFFERS = "user/sellOffers";
    private final String USER_SELLOFFERS_D = "user/sellOffers/ID";
    private final String api = "http://stock-back:8080/";
    private boolean endWork = false;
    // private final String USER_LOGIN = "user/login";
    private long timeBetween = 0;
    public void setEndWork(boolean endWork){
        this.endWork = endWork;
    }

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Async("asyncExecutor")
    public void runTests(UserLogin userLogin, RunTestDTO runTestDTO) throws JSONException,
            JsonProcessingException, InterruptedException {
        int requestsNumber = 0;
        timeBetween = runTestDTO.getTimeBetweenRequests();
        Gson gson = new Gson();
        JSONObject jsonObject;
        ClientTestDTO clientTestDTO = new ClientTestDTO();
        String resources = getResources(userLogin.getJwt(), clientTestDTO);
        jsonObject = new JSONObject(resources);
        Type stockListType = new TypeToken<ArrayList<Stock>>(){}.getType();
        List<Stock> stocks = gson.fromJson(jsonObject.get("stock").toString(), stockListType);
        if(stocks.isEmpty())
            createCompany(userLogin.getJwt(), clientTestDTO);
        Random randomGenerator = new Random();
        int randomStrategy = randomGenerator.nextInt() % 3;
        if (randomStrategy == 0) {
        	boolean buy = true;
            for(;;) {
            	if(buy)
            		sellAllStocks(userLogin.getJwt(), clientTestDTO);
            	else
            		buyStocksUntilHaveMoney(userLogin.getJwt(), clientTestDTO);              
                requestsNumber++;
                buy = !buy;
                if(endWork || requestsNumber >= runTestDTO.getRequestsNumber()){
                	synchronized (lock) {
                		TestController.processNumber--;
                	}
                    return;
                }
                Thread.sleep(runTestDTO.getTimeBetweenRequests());
            }
        } else if (randomStrategy == 1) {
        	boolean buy = true;
        	if(buy) // TODO
        		sellOneStock(userLogin.getJwt(), clientTestDTO);
        	else
        		buyOneStock(userLogin.getJwt(), clientTestDTO);              
            requestsNumber++;
            buy = !buy;
            if(endWork || requestsNumber >= runTestDTO.getRequestsNumber()){
            	synchronized (lock) {
            		TestController.processNumber--;
            	}
                return;
            }
            Thread.sleep(runTestDTO.getTimeBetweenRequests());
        } else {
        	// przeglądanie
            for(;;) {
                double random = Math.random();
                if(random <= runTestDTO.getDataCheck()){
                    getBuyOffers(userLogin.getJwt(), clientTestDTO);
                } else if(random > runTestDTO.getCheckBuyOffers()
                        && random <= runTestDTO.getCheckBuyOffers()
                        + runTestDTO.getCheckSellOffers()){
                    getSellOffers(userLogin.getJwt(), clientTestDTO);
                } else {
                    getUser(userLogin.getJwt(), clientTestDTO);
                }
                requestsNumber++;
                if(endWork || requestsNumber >= runTestDTO.getRequestsNumber()){
                	synchronized (lock) {
                		TestController.processNumber--;
                	}
                    return;
                }
                Thread.sleep(runTestDTO.getTimeBetweenRequests());
            }
        }
    }
    
	private void sellOneStock(String jwt, ClientTestDTO clientTestDTO) throws JSONException, JsonProcessingException, InterruptedException {
	    Gson gson = new Gson();
	    String resources = getResources(jwt, clientTestDTO);
	    if(resources == null) return;
	    JSONObject jsonObject = new JSONObject(resources);
	    Type stockListType = new TypeToken<ArrayList<Stock>>(){}.getType();
	    List<Stock> stocks = gson.fromJson(jsonObject.get("stock").toString(), stockListType);
	    if(stocks.size() <= 0) return;
	    
	    int randomStock = (int) Math.random() * 100 % stocks.size();
	    Stock stock = stocks.get(randomStock);
	    int stockAmountToSell = stock.getAmount();
	    Company company = stock.getCompany();
	    String stockR = getStockRates(jwt, clientTestDTO);
	    if(stockR == null) return;
	    jsonObject = new JSONObject(stockR);
	    Type stockRateListType = new TypeToken<ArrayList<StockRate>>(){}.getType();
	    List<StockRate> stockRates = gson.fromJson(jsonObject.get("stockRate")
	            .toString(), stockRateListType);
	    StockRate stockRateTemp = new StockRate();
	    stockRateTemp.setCompany(company);
	    int stockRateNum = stockRates.indexOf(stockRateTemp);
	    StockRate stockRate = stockRates.get(stockRateNum);
	    double rate = stockRate.getRate();
	    double price = round((Math.abs(new Random().nextDouble()) % (rate * 0.3)
	            + rate * 0.8), 2);
	    createSellOffer(jwt, company.getId(), stockAmountToSell, price, clientTestDTO);
	    if(endWork) return;
	    Thread.sleep(timeBetween);
	    if(endWork) return;
	}

    private void sellAllStocks(String jwt, ClientTestDTO clientTestDTO) throws JSONException, JsonProcessingException, InterruptedException {
        Gson gson = new Gson();
        String resources = getResources(jwt, clientTestDTO);
        if(resources == null) return;
        JSONObject jsonObject = new JSONObject(resources);
        Type stockListType = new TypeToken<ArrayList<Stock>>(){}.getType();
        List<Stock> stocks = gson.fromJson(jsonObject.get("stock").toString(), stockListType);
        if(stocks.size() <= 0) return;
        
        for(Stock stock: stocks) {
        	int stockAmountToSell = stock.getAmount();
        	Company company = stock.getCompany();
        	String stockR = getStockRates(jwt, clientTestDTO);
            if(stockR == null) break;
            jsonObject = new JSONObject(stockR);
            Type stockRateListType = new TypeToken<ArrayList<StockRate>>(){}.getType();
            List<StockRate> stockRates = gson.fromJson(jsonObject.get("stockRate")
                    .toString(), stockRateListType);
            StockRate stockRateTemp = new StockRate();
            stockRateTemp.setCompany(company);
            int stockRateNum = stockRates.indexOf(stockRateTemp);
            StockRate stockRate = stockRates.get(stockRateNum);
            double rate = stockRate.getRate();
            double price = round((Math.abs(new Random().nextDouble()) % (rate * 0.3)
                    + rate * 0.8), 2);
            createSellOffer(jwt, company.getId(), stockAmountToSell, price, clientTestDTO);
            if(endWork) return;
            Thread.sleep(timeBetween);
            if(endWork) return;
        }
    }
    
    private void buyOneStock(String jwt, ClientTestDTO clientTestDTO) throws JSONException, JsonProcessingException, InterruptedException {
        Gson gson = new Gson();
        String temp = getUser(jwt, clientTestDTO);
        if(temp == null) return;
        JSONObject jsonObject = new JSONObject(temp);
        User user = gson.fromJson(jsonObject.get("user").toString(), User.class);
        double money = user.getMoney();
        jsonObject = new JSONObject(getCompanies(jwt, clientTestDTO));
        Type companyListType = new TypeToken<ArrayList<Company>>(){}.getType();
        List<Company> companies = gson.fromJson(jsonObject.get("company").toString(), companyListType);
        double price;
        int amount;
        String stockR = getStockRates(jwt, clientTestDTO);
        if(stockR == null) return;
        jsonObject = new JSONObject(stockR);
        Type stockRateListType = new TypeToken<ArrayList<StockRate>>(){}.getType();
        List<StockRate> stockRates = gson.fromJson(jsonObject.get("stockRate")
                .toString(), stockRateListType);
        StockRate stockRateTemp = new StockRate();
        int randomCompany = (int) Math.random() * 100 % companies.size();
        Company company = companies.get(randomCompany);
	    stockRateTemp.setCompany(company);
	    int stockRateNum = stockRates.indexOf(stockRateTemp);
	    StockRate stockRate = stockRates.get(stockRateNum);
	    double rate = stockRate.getRate();
	    price = round((Math.abs(new Random().nextDouble()) % (rate * 0.3) + rate * 0.9), 2);
	    amount = (int) Math.round(Math.random() * 100.f % (money / price / 10));
	    createBuyOffer(jwt, company.getId(), amount, price, clientTestDTO);
	    if(endWork) return;
	    Thread.sleep(timeBetween);
	    if(endWork) return;
    }

    private void buyStocksUntilHaveMoney(String jwt, ClientTestDTO clientTestDTO) throws JSONException, JsonProcessingException, InterruptedException {
        Gson gson = new Gson();
        String temp = getUser(jwt, clientTestDTO);
        if(temp == null) return;
        JSONObject jsonObject = new JSONObject(temp);
        User user = gson.fromJson(jsonObject.get("user").toString(), User.class);
        double money = user.getMoney();
        jsonObject = new JSONObject(getCompanies(jwt, clientTestDTO));
        Type companyListType = new TypeToken<ArrayList<Company>>(){}.getType();
        List<Company> companies = gson.fromJson(jsonObject.get("company").toString(), companyListType);
        double price;
        int amount;
        String stockR = getStockRates(jwt, clientTestDTO);
        if(stockR == null) return;
        jsonObject = new JSONObject(stockR);
        Type stockRateListType = new TypeToken<ArrayList<StockRate>>(){}.getType();
        List<StockRate> stockRates = gson.fromJson(jsonObject.get("stockRate")
                .toString(), stockRateListType);
        StockRate stockRateTemp = new StockRate();
        mainLoop:
        for(;;) {
	        for(Company company: companies) {
	        	stockRateTemp.setCompany(company);
	            int stockRateNum = stockRates.indexOf(stockRateTemp);
	            StockRate stockRate = stockRates.get(stockRateNum);
	            double rate = stockRate.getRate();
	            price = round((Math.abs(new Random().nextDouble()) % (rate * 0.3) + rate * 0.9), 2);
	            amount = (int) Math.round(Math.random() * 100.f % (money / price / 10));
	            if(amount == 0) break mainLoop;
	            createBuyOffer(jwt, company.getId(), amount, price, clientTestDTO);
	            money -= price*amount;
	            if(endWork) return;
	            Thread.sleep(timeBetween);
	            if(endWork) return;
	        }
	    }
    }

   
    public void deleteSellOffer(String jwt, ClientTestDTO clientTestDTO) throws JSONException {
        JSONObject jsonObject;
        Gson gson = new Gson();
        String temp = getSellOffers(jwt, clientTestDTO);
        if(temp == null) return;
        jsonObject = new JSONObject(temp);
        Type sellOfferListType = new TypeToken<ArrayList<SellOffer>>(){}.getType();
        List<SellOffer> sellOffers = gson.fromJson(jsonObject.get("sellOffers").toString(), sellOfferListType);
        if(sellOffers.size() == 0) return;
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity entity = new HttpEntity(headers);
        int i = Math.abs(new Random().nextInt()) % sellOffers.size();
        long apiTime = System.currentTimeMillis();
        restTemplate.exchange(
                this.api + "api/user/sellOffers/" + sellOffers.get(i).getId(), HttpMethod.DELETE, entity, String.class, new Object());
        apiTime = System.currentTimeMillis() - apiTime;
        TestDetailsDTO testDetailsDTO = gson.fromJson(jsonObject.get("testDetails").toString(), TestDetailsDTO.class);
        clientTestDTO.addTestDetails(USER_SELLOFFERS_D, testDetailsDTO, apiTime);
    }

    public void deleteBuyOffer(String jwt, ClientTestDTO clientTestDTO) throws JSONException {
        JSONObject jsonObject;
        Gson gson = new Gson();
        String temp = getBuyOffers(jwt, clientTestDTO);
        if(temp == null) return;
        jsonObject = new JSONObject();
        Type buyOfferListType = new TypeToken<ArrayList<BuyOffer>>(){}.getType();
        List<BuyOffer> buyOffers;
        if(jsonObject.has("buyOffers")) {
            buyOffers = gson.fromJson(jsonObject.get("buyOffers").toString(), buyOfferListType);
        } else return;
        if(buyOffers.size() == 0) return;
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity entity = new HttpEntity(headers);
        int i = Math.abs(new Random().nextInt()) % buyOffers.size();
        long apiTime = System.currentTimeMillis();
        restTemplate.exchange(
                this.api + "api/user/buyOffers/" + buyOffers.get(i).getId(), HttpMethod.DELETE, entity, String.class, new Object());
        apiTime = System.currentTimeMillis() - apiTime;
        TestDetailsDTO testDetailsDTO = gson.fromJson(jsonObject.get("testDetails").toString(), TestDetailsDTO.class);
        clientTestDTO.addTestDetails(USER_BUYOFFERS_D, testDetailsDTO, apiTime);
    }

    public void postRegistration(String username) throws JSONException {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject personJsonObject;
        personJsonObject = new JSONObject();
        personJsonObject.put("id", 0);
        personJsonObject.put("name", "John");
        personJsonObject.put("surname", "Johnny");
        personJsonObject.put("username", username);
        personJsonObject.put("email", "mail@mail.pl");
        personJsonObject.put("password", "testpassword");
        HttpEntity<String> request =
                new HttpEntity<>(personJsonObject.toString(), headers);
        String message = "";
        try {
            message = restTemplate.postForObject(this.api
                    + "api/auth/signup", request, String.class);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public UserLogin login(String username) throws JSONException {
        ////log.info("Login " + username + " starts");
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject personJsonObject;
        personJsonObject = new JSONObject();
        personJsonObject.put("username", username);
        personJsonObject.put("password", "testpassword");
        HttpEntity<String> request = new HttpEntity<>(personJsonObject.toString(), headers);
        UserLogin userLogin = null;
        try {
            userLogin = restTemplate.postForObject(this.api + "api/auth/signin", request, UserLogin.class);
            ////log.info("Login " + username + " ends");
        } catch (Exception e){
            ////log.info("Error login " + username);
        }
        return userLogin;
    }

    private String getUser(String jwt, ClientTestDTO clientTestDTO) throws JSONException {
        Gson gson = new Gson();
        JSONObject jsonObject;
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity jsonResponse = null;
        long apiTime = System.currentTimeMillis();
        try {
            jsonResponse = restTemplate.exchange(
                    this.api + "api/user", HttpMethod.GET, entity, String.class, new Object());
        } catch (Exception e){
            ////log.info("Error " + e);
        }
        apiTime = System.currentTimeMillis() - apiTime;
        String response = null;
        if(jsonResponse != null) {
            response = (String) jsonResponse.getBody();
            jsonObject = new JSONObject(response);
            TestDetailsDTO testDetailsDTO = gson.fromJson(jsonObject.get("testDetails").toString(), TestDetailsDTO.class);
            clientTestDTO.addTestDetails(USER, testDetailsDTO, apiTime);
        }
        return response;
    }

    private String getBuyOffers(String jwt, ClientTestDTO clientTestDTO) throws JSONException {
        Gson gson = new Gson();
        JSONObject jsonObject;
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity jsonResponse = null;
        long apiTime = System.currentTimeMillis();
        try {
            jsonResponse = restTemplate.exchange(
                    this.api + "api/user/buyOffers", HttpMethod.GET, entity, String.class, new Object());
        } catch (Exception e){
            ////log.info("Error " + e);
        }
        apiTime = System.currentTimeMillis() - apiTime;
        String response = null;
        if(jsonResponse != null) {
            response = (String) jsonResponse.getBody();
            jsonObject = new JSONObject(response);
            TestDetailsDTO testDetailsDTO = gson.fromJson(jsonObject.get("testDetails").toString(), TestDetailsDTO.class);
            clientTestDTO.addTestDetails(USER_BUYOFFERS, testDetailsDTO, apiTime);
        }
        return response;
    }

    private String getSellOffers(String jwt, ClientTestDTO clientTestDTO) throws JSONException {
        Gson gson = new Gson();
        JSONObject jsonObject;
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity jsonResponse = null;
        long apiTime = System.currentTimeMillis();
        try {
            jsonResponse = restTemplate.exchange(
                    this.api + "api/user/sellOffers", HttpMethod.GET, entity, String.class, new Object());
        } catch (Exception e){
            ////log.info("Error " + e);
        }
        apiTime = System.currentTimeMillis() - apiTime;
        String response = null;
        if(jsonResponse != null) {
            response = (String) jsonResponse.getBody();
            jsonObject = new JSONObject(response);
            TestDetailsDTO testDetailsDTO = gson.fromJson(jsonObject.get("testDetails").toString(), TestDetailsDTO.class);
            clientTestDTO.addTestDetails(USER_SELLOFFERS, testDetailsDTO, apiTime);
        }
        return response;
    }

    private String getResources(String jwt, ClientTestDTO clientTestDTO) throws JSONException {
        Gson gson = new Gson();
        JSONObject jsonObject;
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity jsonResponse = null;
        long apiTime = System.currentTimeMillis();
        try {
            jsonResponse = restTemplate.exchange(
                    this.api + "api/user/resources", HttpMethod.GET, entity, String.class, new Object());
        } catch (Exception e){
            //log.info("Error " + e);
        }
        apiTime = System.currentTimeMillis() - apiTime;
        String response = null;
        if(jsonResponse != null) {
            response = (String) jsonResponse.getBody();
            jsonObject = new JSONObject(response);
            TestDetailsDTO testDetailsDTO = gson.fromJson(jsonObject.get("testDetails").toString(), TestDetailsDTO.class);
            clientTestDTO.addTestDetails(USER_RESOURCES, testDetailsDTO, apiTime);
        }
        return response;
    }

    private String getStockRates(String jwt, ClientTestDTO clientTestDTO) throws JSONException {
        Gson gson = new Gson();
        JSONObject jsonObject;
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity jsonResponse = null;
        long apiTime = System.currentTimeMillis();
        try {
            jsonResponse = restTemplate.exchange(
                    this.api + "stockRates", HttpMethod.GET, entity, String.class, new Object());
        } catch (Exception e){
            //log.info("Error " + e);
        }
        apiTime = System.currentTimeMillis() - apiTime;
        if(jsonResponse == null) return null;
        jsonObject = new JSONObject((String) jsonResponse.getBody());
        TestDetailsDTO testDetailsDTO = gson.fromJson(jsonObject.get("testDetails").toString(), TestDetailsDTO.class);
        clientTestDTO.addTestDetails(STOCKRATES, testDetailsDTO, apiTime);
        return (String) jsonResponse.getBody();
    }

    private void createCompany(String jwt, ClientTestDTO clientTestDTO) throws JSONException {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt);
        JSONObject companyJsonObject;
        companyJsonObject = new JSONObject();
        companyJsonObject.put("id", "0");
        // Generate company name
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 7;
        Random random = new Random();
        StringBuilder sb = new StringBuilder(targetStringLength);
        for(int i = 0; i < targetStringLength; i++){
            int randomLimitedInt = leftLimit + (int) (random.nextDouble() * (rightLimit - leftLimit + 1));
            sb.append((char) randomLimitedInt);
        }
        String name = sb.toString();
        // Generate stock amount/price
        int amount = Math.abs(new Random().nextInt() % 1500) + 50;
        double price = Math.round(new Random().nextDouble() * 10000) / 100.0;
        companyJsonObject.put("name", name);
        companyJsonObject.put("amount", amount);
        companyJsonObject.put("price", price);
        HttpEntity<String> request = new HttpEntity<>(companyJsonObject.toString(), headers);

        TestDetailsDTO testDetailsDTO = null;
        long apiTime = System.currentTimeMillis();
        try {
            testDetailsDTO = restTemplate.postForObject(this.api + "company", request, TestDetailsDTO.class);
        } catch (Exception e){
            //log.info("Error " + e);
        }
        apiTime = System.currentTimeMillis() - apiTime;
        assert testDetailsDTO != null;
        //log.info(testDetailsDTO.toString());
        clientTestDTO.addTestDetails(COMPANY, testDetailsDTO, apiTime);
    }

    private String getCompanies(String jwt, ClientTestDTO clientTestDTO) throws JSONException {
        Gson gson = new Gson();
        JSONObject jsonObject;
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity jsonResponse = null;
        long apiTime = System.currentTimeMillis();
        try {
            jsonResponse = restTemplate.exchange(
                    this.api + "companies", HttpMethod.GET, entity, String.class, new Object());
        } catch (Exception e){
            //log.info("Error " + e);
        }
        apiTime = System.currentTimeMillis() - apiTime;
        assert jsonResponse != null;
        jsonObject = new JSONObject((String) jsonResponse.getBody());
        TestDetailsDTO testDetailsDTO = gson.fromJson(jsonObject.get("testDetails").toString(), TestDetailsDTO.class);
        clientTestDTO.addTestDetails(COMPANIES, testDetailsDTO, apiTime);
        return (String) jsonResponse.getBody();
    }

    private void strategyAddSellOffer(String jwt, int strategy, ClientTestDTO clientTestDTO) throws JSONException, JsonProcessingException, InterruptedException {
        Gson gson = new Gson();
        String resources = getResources(jwt, clientTestDTO);
        if(resources == null) return;
        JSONObject jsonObject = new JSONObject(resources);
        Type stockListType = new TypeToken<ArrayList<Stock>>(){}.getType();
        List<Stock> stocks = gson.fromJson(jsonObject.get("stock").toString(), stockListType);
        if(stocks.size() <= 0) return;
        int stockNum;
        switch(strategy){
            case RAND_EXPENSIVE_ONE_COMP:
                stockNum = Math.abs(new Random().nextInt()
                        % stocks.size());
                double price = Math.random() % 100.0 + 1;
                price = round(price, 2);
                int amount = (int) Math.round(Math.random()
                        * 100.f % (stocks.get(stockNum).getAmount()));
                int companyId = stocks.get(stockNum).getCompany().getId();
                createSellOffer(jwt, companyId, amount, price, clientTestDTO);
                break;
            case RAND_RANDOM_MANY_COMP:
                String stockR = getStockRates(jwt, clientTestDTO);
                if(stockR == null) break;
                jsonObject = new JSONObject(stockR);
                Type stockRateListType = new TypeToken<ArrayList<StockRate>>(){}.getType();
                List<StockRate> stockRates = gson.fromJson(jsonObject.get("stockRate")
                        .toString(), stockRateListType);
                int amountOfStocks = Math.abs(new Random().nextInt() % stocks.size())/3 + 1;
                for(int i = 0; i < amountOfStocks; i++){
                    stockNum = Math.abs(new Random().nextInt() % stocks.size());
                    Stock stock = stocks.get(stockNum);
                    Company company = stock.getCompany();
                    StockRate stockRateTemp = new StockRate();
                    stockRateTemp.setCompany(company);
                    int stockRateNum = stockRates.indexOf(stockRateTemp);
                    StockRate stockRate = stockRates.get(stockRateNum);
                    double rate = stockRate.getRate();
                    price = round((Math.abs(new Random().nextDouble()) % (rate * 0.3)
                            + rate * 0.8), 2);
                    amount = (int) Math.round(Math.random() * 100.f % (stocks.get(stockNum)
                            .getAmount()));
                    if(amount <= 0) amount = 1;
                    createSellOffer(jwt, company.getId(), amount, price, clientTestDTO);
                    stocks.remove(stockNum);
                    if(endWork) return;
                    Thread.sleep(timeBetween);
                    if(endWork) return;
                }
                break;
        }
    }



    private void strategyAddBuyOffer(String jwt, int strategy, ClientTestDTO clientTestDTO) throws JSONException, JsonProcessingException, InterruptedException {
        Gson gson = new Gson();
        String temp = getUser(jwt, clientTestDTO);
        if(temp == null) return;
        JSONObject jsonObject = new JSONObject(temp);
        User user = gson.fromJson(jsonObject.get("user").toString(), User.class);
        jsonObject = new JSONObject(getCompanies(jwt, clientTestDTO));
        Type companyListType = new TypeToken<ArrayList<Company>>(){}.getType();
        List<Company> companies = gson.fromJson(jsonObject.get("company").toString(), companyListType);
        double price;
        int amount;
        switch(strategy){
            case RAND_EXPENSIVE_ONE_COMP:
                price = Math.random() * 100.f % (user.getMoney()/4.f);
                price = round(price, 2);
                amount = (int) Math.round(Math.random() * 100.f % (user.getMoney() / price / 2));
                if(amount == 0) amount = 1;
                int companyId = companies.get((int) Math.round(Math.random() * 100.f % (companies.size() - 1))).getId();
                createBuyOffer(jwt, companyId, amount, price, clientTestDTO);
                break;
            case RAND_RANDOM_MANY_COMP:
                String stockR = getStockRates(jwt, clientTestDTO);
                if(stockR == null) break;
                jsonObject = new JSONObject(stockR);
                Type stockRateListType = new TypeToken<ArrayList<StockRate>>(){}.getType();
                List<StockRate> stockRates = gson.fromJson(jsonObject.get("stockRate")
                        .toString(), stockRateListType);
                if(stockRates.size() / 3 == 0) return;
                int amountOfCompanies = Math.abs(new Random().nextInt() % (stockRates.size() / 3)) + 1;
                for(int i = 0; i < amountOfCompanies; i++){
                    int companyNum = Math.abs(new Random().nextInt() % stockRates.size());
                    StockRate stockRate = stockRates.get(companyNum);
                    Company company = stockRate.getCompany();
                    double rate = stockRate.getRate();
                    price = round((Math.abs(new Random().nextDouble()) % (rate * 0.3) + rate * 0.9), 2);
                    amount = (int) Math.round(Math.random() * 100.f % (user.getMoney() / price / 10));
                    if(amount == 0) amount = 1;
                    createBuyOffer(jwt, company.getId(), amount, price, clientTestDTO);
                    stockRates.remove(companyNum);
                    if(endWork) return;
                    Thread.sleep(timeBetween);
                    if(endWork) return;
                }
                break;
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    private void createBuyOffer(String jwt, int companyId, int amount, double price, ClientTestDTO clientTestDTO) throws JSONException {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt);
        JSONObject companyJsonObject;
        companyJsonObject = new JSONObject();
        companyJsonObject.put("id", "0");
        companyJsonObject.put("company_id", companyId);
        companyJsonObject.put("maxPrice", price);
        companyJsonObject.put("amount", amount);
        companyJsonObject.put("dateLimit", "2014-05-09T00:48:16-04:00");
        HttpEntity<String> request = new HttpEntity<>(companyJsonObject.toString(), headers);
        ////log.info("CREATING A BUY OFFER");
        TestDetailsDTO testDetailsDTO = null;
        long apiTime = System.currentTimeMillis();
        try {
            testDetailsDTO = restTemplate.postForObject( this.api + "api/buyOffer", request, TestDetailsDTO.class);
        } catch (Exception e){
            // //log.info("Error " + e);
        }
        apiTime = System.currentTimeMillis() - apiTime;
        if(testDetailsDTO !=null)
            clientTestDTO.addTestDetails(BUYOFFER, testDetailsDTO, apiTime);
    }

    private void createSellOffer(String jwt, int companyId, int amount,
                                 double price, ClientTestDTO clientTestDTO)
            throws JSONException {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt);
        JSONObject companyJsonObject;
        companyJsonObject = new JSONObject();
        companyJsonObject.put("id", "0");
        companyJsonObject.put("company_id", companyId);
        companyJsonObject.put("minPrice", price);
        companyJsonObject.put("amount", amount);
        companyJsonObject.put("dateLimit", "2014-05-09T00:48:16-04:00");
        HttpEntity<String> request = new HttpEntity<>
                (companyJsonObject.toString(), headers);
        TestDetailsDTO testDetailsDTO = null;
        long apiTime = System.currentTimeMillis();
        try {
            testDetailsDTO = restTemplate.postForObject
                    (this.api + "api/sellOffer", request, TestDetailsDTO.class);
        } catch (Exception e){
            e.printStackTrace();
        }
        apiTime = System.currentTimeMillis() - apiTime;
        if(testDetailsDTO != null)
            clientTestDTO.addTestDetails(SELLOFFER, testDetailsDTO, apiTime);
    }
}




















