package com.raphau.trafficgenerator.dto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Min;

@Entity
public class RunTestDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Min(20)
    private int timeBetweenRequests;
    @Min(1)
    private int testTime;
    @Min(1)
    private int requestsNumber;
    @Min(0)
    private int first;
    @Min(0)
    private int second;
    @Min(0)
    private int third;
    private boolean requestLimit;
    private boolean timeLimit;
    private int breakBetweenTrades;

    public RunTestDTO() {
    }

    public RunTestDTO(int timeBetweenRequests, int testTime, int requestsNumber, int first, int second, int third, boolean requestLimit, boolean timeLimit, int breakBetweenTrades) {
        this.timeBetweenRequests = timeBetweenRequests;
        this.testTime = testTime;
        this.requestsNumber = requestsNumber;
        this.first = first;
        this.second = second;
        this.third = third;
        this.requestLimit = requestLimit;
        this.timeLimit = timeLimit;
        this.breakBetweenTrades = breakBetweenTrades;
    }

    public int getBreakBetweenTrades() {
        return breakBetweenTrades;
    }

    public void setBreakBetweenTrades(int breakBetweenTrades) {
        this.breakBetweenTrades = breakBetweenTrades;
    }

    public boolean isRequestLimit() {
        return requestLimit;
    }

    public void setRequestLimit(boolean requestLimit) {
        this.requestLimit = requestLimit;
    }

    public boolean isTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(boolean timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getTestTime() {
        return testTime;
    }

    public void setTestTime(int testTime) {
        this.testTime = testTime;
    }

    public int getRequestsNumber() {
        return requestsNumber;
    }

    public void setRequestsNumber(int requestsNumber) {
        this.requestsNumber = requestsNumber;
    }

    public int getTimeBetweenRequests() {
        return timeBetweenRequests;
    }

    public void setTimeBetweenRequests(int timeBetweenRequests) {
        this.timeBetweenRequests = timeBetweenRequests;
    }


    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getThird() {
        return third;
    }

    public void setThird(int third) {
        this.third = third;
    }

    @Override
    public String toString() {
        return "RunTestDTO{" +
                "id=" + id +
                ", timeBetweenRequests=" + timeBetweenRequests +
                ", testTime=" + testTime +
                ", requestsNumber=" + requestsNumber +
                ", first=" + first +
                ", second=" + second +
                ", third=" + third +
                ", requestLimit=" + requestLimit +
                ", timeLimit=" + timeLimit +
                '}';
    }
}
