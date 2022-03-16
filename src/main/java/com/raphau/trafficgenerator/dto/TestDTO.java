package com.raphau.trafficgenerator.dto;

import com.raphau.trafficgenerator.entity.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestDTO {

    private int id;
    private String name;
    private final SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    private Date startTimestamp;
    private Date endTimestamp;
    private long testTime;
    private String startDate;
    private String endDate;
    private boolean finished;

    public TestDTO() {
    }

    public TestDTO(int id, String name, Date timestamp, boolean finished) {
        this.id = id;
        this.name = name;
        this.startTimestamp = timestamp;
        this.finished = finished;
    }

    public TestDTO(Test test) {
        this.id = test.getId();
        this.name = test.getName();
        this.startTimestamp = new Date(test.getStartTimestamp());
        this.startDate = simpleDateFormat.format(this.startTimestamp);
        if(test.getEndTimestamp() != null){
            this.endTimestamp = new Date(test.getEndTimestamp());
            this.endDate = simpleDateFormat.format(this.endTimestamp);
        }
        this.finished = test.isFinished();
        if(test.getTestParameters() != null)
            this.testTime = test.getTestParameters().getTest_time();
    }

    public long getTestTime() {
        return testTime;
    }

    public void setTestTime(long testTime) {
        this.testTime = testTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Date getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Date startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public SimpleDateFormat getSimpleDateFormat() {
        return simpleDateFormat;
    }

    public String getStartDate() {
        return startDate;
    }

    public Date getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Date endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return "TestDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", simpleDateFormat=" + simpleDateFormat +
                ", timestamp=" + startTimestamp +
                ", testTime=" + testTime +
                ", date='" + startDate + '\'' +
                ", finished=" + finished +
                '}';
    }
}
