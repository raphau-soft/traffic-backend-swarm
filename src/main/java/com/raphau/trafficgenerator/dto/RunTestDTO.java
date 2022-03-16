package com.raphau.trafficgenerator.dto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Entity
public class RunTestDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Min(20)
    private int timeBetweenRequests;
    @Min(1)
    private int numberOfUsers;
    @Min(1000)
    private int testTime;
    @Min(1)
    private int requestsNumber;

    public RunTestDTO() {
    }

    public RunTestDTO(int timeBetweenRequests, int numberOfUsers,
                      int testTime, int requestsNumber) {
        this.timeBetweenRequests = timeBetweenRequests;
        this.numberOfUsers = numberOfUsers;
        this.testTime = testTime;
        this.requestsNumber = requestsNumber;
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

    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    public void setNumberOfUsers(int numberOfUsers) {
        this.numberOfUsers = numberOfUsers;
    }
}
