package com.raphau.trafficgenerator.dto;

import com.raphau.trafficgenerator.entity.Test;

public class TrafficGeneratorTimeDataDTO {

    private int id;
    private Test test;
    private long applicationTime;
    private long databaseTime;
    private long timestamp;
    private String endpointUrl;
    private String method;
	private String stockId;

	public String getStockId() {
		return stockId;
	}

	public void setStockId(String stockId) {
		this.stockId = stockId;
	}

	public TrafficGeneratorTimeDataDTO() {
    }

	public TrafficGeneratorTimeDataDTO(int id, Test test, long applicationTime, long databaseTime, long timestamp, String endpointUrl, String method, String stockId) {
		this.id = id;
		this.test = test;
		this.applicationTime = applicationTime;
		this.databaseTime = databaseTime;
		this.timestamp = timestamp;
		this.endpointUrl = endpointUrl;
		this.method = method;
		this.stockId = stockId;
	}

	public TrafficGeneratorTimeDataDTO(int id, Test test, long appTime, long dbTime, long timestamp, String endpointUrl,
									   String method) {
		super();
		this.id = id;
		this.test = test;
		this.applicationTime = appTime;
		this.databaseTime = dbTime;
		this.timestamp = timestamp;
		this.endpointUrl = endpointUrl;
		this.method = method;
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

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
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
				", method='" + method + '\'' +
				'}';
	}
}
