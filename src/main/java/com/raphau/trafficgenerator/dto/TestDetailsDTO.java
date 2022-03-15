package com.raphau.trafficgenerator.dto;

public class TestDetailsDTO {

    private String action;
    private long databaseTime;
    private long applicationTime;

    public TestDetailsDTO() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getDatabaseTime() {
        return databaseTime;
    }

    public void setDatabaseTime(long databaseTime) {
        this.databaseTime = databaseTime;
    }

    public long getApplicationTime() {
        return applicationTime;
    }

    public void setApplicationTime(long applicationTime) {
        this.applicationTime = applicationTime;
    }

    @Override
    public String toString() {
        return "TestDetailsDTO{" +
                "action='" + action + '\'' +
                ", databaseTime=" + databaseTime +
                ", applicationTime=" + applicationTime +
                '}';
    }
}
