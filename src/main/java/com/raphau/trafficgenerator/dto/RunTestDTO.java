package com.raphau.trafficgenerator.dto;

public class RunTestDTO {

    private long timeBetweenRequests;
    private long numberOfUsers;
    private double stockPlay;
    private double createBuyOffer;
    private double createSellOffer;
    private double deleteSellOffer;
    private double deleteBuyOffer;
    private double dataCheck;
    private double checkBuyOffers;
    private double checkSellOffers;
    private double checkUserData;
    private int strategy;
    private long testTime;
    private int requestsNumber;

    public RunTestDTO() {
    }

    public RunTestDTO(long timeBetweenRequests, long numberOfUsers, double stockPlay,
                      double createBuyOffer, double createSellOffer, double deleteSellOffer,
                      double deleteBuyOffer, double dataCheck,
                      double checkBuyOffers, double checkSellOffers, double checkUserData,
                      int strategy, long testTime, int requestsNumber) {
        this.timeBetweenRequests = timeBetweenRequests;
        this.numberOfUsers = numberOfUsers;
        this.stockPlay = stockPlay;
        this.createBuyOffer = createBuyOffer;
        this.createSellOffer = createSellOffer;
        this.deleteSellOffer = deleteSellOffer;
        this.deleteBuyOffer = deleteBuyOffer;
        this.dataCheck = dataCheck;
        this.checkBuyOffers = checkBuyOffers;
        this.checkSellOffers = checkSellOffers;
        this.checkUserData = checkUserData;
        this.strategy = strategy;
        this.testTime = testTime;
        this.requestsNumber = requestsNumber;
    }

    public long getTestTime() {
        return testTime;
    }

    public void setTestTime(long testTime) {
        this.testTime = testTime;
    }

    public int getRequestsNumber() {
        return requestsNumber;
    }

    public void setRequestsNumber(int requestsNumber) {
        this.requestsNumber = requestsNumber;
    }

    public long getTimeBetweenRequests() {
        return timeBetweenRequests;
    }

    public void setTimeBetweenRequests(long timeBetweenRequests) {
        this.timeBetweenRequests = timeBetweenRequests;
    }

    public long getNumberOfUsers() {
        return numberOfUsers;
    }

    public void setNumberOfUsers(long numberOfUsers) {
        this.numberOfUsers = numberOfUsers;
    }

    public double getStockPlay() {
        return stockPlay;
    }

    public void setStockPlay(double stockPlay) {
        this.stockPlay = stockPlay;
    }

    public double getCreateBuyOffer() {
        return createBuyOffer;
    }

    public void setCreateBuyOffer(double createBuyOffer) {
        this.createBuyOffer = createBuyOffer;
    }

    public double getCreateSellOffer() {
        return createSellOffer;
    }

    public void setCreateSellOffer(double createSellOffer) {
        this.createSellOffer = createSellOffer;
    }

    public double getDeleteSellOffer() {
        return deleteSellOffer;
    }

    public void setDeleteSellOffer(double deleteSellOffer) {
        this.deleteSellOffer = deleteSellOffer;
    }

    public double getDeleteBuyOffer() {
        return deleteBuyOffer;
    }

    public void setDeleteBuyOffer(double deleteBuyOffer) {
        this.deleteBuyOffer = deleteBuyOffer;
    }

    public double getDataCheck() {
        return dataCheck;
    }

    public void setDataCheck(double dataCheck) {
        this.dataCheck = dataCheck;
    }

    public double getCheckBuyOffers() {
        return checkBuyOffers;
    }

    public void setCheckBuyOffers(double checkBuyOffers) {
        this.checkBuyOffers = checkBuyOffers;
    }

    public double getCheckSellOffers() {
        return checkSellOffers;
    }

    public void setCheckSellOffers(double checkSellOffers) {
        this.checkSellOffers = checkSellOffers;
    }

    public double getCheckUserData() {
        return checkUserData;
    }

    public void setCheckUserData(double checkUserData) {
        this.checkUserData = checkUserData;
    }

    public int getStrategy() {
        return strategy;
    }

    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }
}
