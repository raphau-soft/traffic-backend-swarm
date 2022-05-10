package com.raphau.trafficgenerator.dto;

import com.raphau.trafficgenerator.entity.Test;

public class TrafficGeneratorTimeDataDTO {

    private int id;
    private Test test;
    private long apiTime;
    private long applicationTime;
    private long databaseTime;
    private long timestamp;
	private int queueSizeBack;
    private String endpointUrl;
	private String stockId;

	public String getStockId() {
		return stockId;
	}

	public void setStockId(String stockId) {
		this.stockId = stockId;
	}

	public TrafficGeneratorTimeDataDTO() {
    }

	public TrafficGeneratorTimeDataDTO(int id, Test test, long apiTime, long applicationTime, long databaseTime, long timestamp, String endpointUrl, String stockId) {
		this.id = id;
		this.test = test;
		this.apiTime = apiTime;
		this.applicationTime = applicationTime;
		this.databaseTime = databaseTime;
		this.timestamp = timestamp;
		this.endpointUrl = endpointUrl;
		this.stockId = stockId;
	}

	public TrafficGeneratorTimeDataDTO(int id, Test test, long apiTime, long appTime, long dbTime, long timestamp, String endpointUrl) {
		super();
		this.id = id;
		this.test = test;
		this.apiTime = apiTime;
		this.applicationTime = appTime;
		this.databaseTime = dbTime;
		this.timestamp = timestamp;
		this.endpointUrl = endpointUrl;
	}

	public TrafficGeneratorTimeDataDTO(int id, Test test, long apiTime, long applicationTime, long databaseTime, long timestamp, int queueSizeBack, String endpointUrl, String stockId) {
		this.id = id;
		this.test = test;
		this.apiTime = apiTime;
		this.applicationTime = applicationTime;
		this.databaseTime = databaseTime;
		this.timestamp = timestamp;
		this.queueSizeBack = queueSizeBack;
		this.endpointUrl = endpointUrl;
		this.stockId = stockId;
	}

	public int getQueueSizeBack() {
		return queueSizeBack;
	}

	public void setQueueSizeBack(int queueSizeBack) {
		this.queueSizeBack = queueSizeBack;
	}

	public long getApiTime() {
		return apiTime;
	}

	public void setApiTime(long apiTime) {
		this.apiTime = apiTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Test getTest() {
		return test;
	}

	public void setTest(Test test) {
		this.test = test;
	}

	public long getApplicationTime() {
		return applicationTime;
	}

	public void setApplicationTime(long applicationTime) {
		this.applicationTime = applicationTime;
	}

	public long getDatabaseTime() {
		return databaseTime;
	}

	public void setDatabaseTime(long databaseTime) {
		this.databaseTime = databaseTime;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getEndpointUrl() {
		return endpointUrl;
	}

	public void setEndpointUrl(String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}

	@Override
	public String toString() {
		return "TrafficGeneratorTimeDataDTO{" +
				"id=" + id +
				", test=" + test +
				", appTime=" + applicationTime +
				", dbTime=" + databaseTime +
				", timestamp=" + timestamp +
				", endpointUrl='" + endpointUrl + '\'' +
				'}';
	}
}
