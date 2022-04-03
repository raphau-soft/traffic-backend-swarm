package com.raphau.trafficgenerator.entity;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.raphau.trafficgenerator.dto.TrafficGeneratorTimeDataDTO;

@Entity
@Table(name="traffic_generator_time_data", schema="traffic_generator")
public class TrafficGeneratorTimeData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "test_id", referencedColumnName = "id")
    @JsonBackReference
    private Test test;

    @Column(name = "application_time")
    private Long applicationTime;

    @Column(name = "database_time")
    private Long databaseTime;
    
    @Column(name = "timestamp")
    private Long timestamp;
    
    @Column(name = "endpoint_url")
    private String endpointUrl;

    @Column(name = "method")
    private String method;

	@Column(name = "stock_id")
	private String stockId;
    
    public TrafficGeneratorTimeData() {
    }

	public TrafficGeneratorTimeData(int id, Test test, Long applicationTime, Long databaseTime, Long timestamp, String endpointUrl, String method, String stockId) {
		this.id = id;
		this.test = test;
		this.applicationTime = applicationTime;
		this.databaseTime = databaseTime;
		this.timestamp = timestamp;
		this.endpointUrl = endpointUrl;
		this.method = method;
		this.stockId = stockId;
	}

	public void setApplicationTime(Long applicationTime) {
		this.applicationTime = applicationTime;
	}

	public void setDatabaseTime(Long databaseTime) {
		this.databaseTime = databaseTime;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getStockId() {
		return stockId;
	}

	public void setStockId(String stockId) {
		this.stockId = stockId;
	}

	public TrafficGeneratorTimeData(int id, Test test, Long appTime, Long dbTime, Long timestamp, String endpointUrl,
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

	public TrafficGeneratorTimeData(TrafficGeneratorTimeDataDTO trafficGeneratorTimeDataDTO, Test test) {
		super();
		this.id = trafficGeneratorTimeDataDTO.getId();
		this.test = test;
		this.applicationTime = trafficGeneratorTimeDataDTO.getApplicationTime();
		this.databaseTime = trafficGeneratorTimeDataDTO.getDatabaseTime();
		this.timestamp = trafficGeneratorTimeDataDTO.getTimestamp();
		this.endpointUrl = trafficGeneratorTimeDataDTO.getEndpointUrl();
		this.method = trafficGeneratorTimeDataDTO.getMethod();
		this.stockId = trafficGeneratorTimeDataDTO.getStockId();
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

	public Long getApplicationTime() {
		return applicationTime;
	}

	public void setApplicationTime(long appTime) {
		this.applicationTime = appTime;
	}

	public Long getDatabaseTime() {
		return databaseTime;
	}

	public void setDatabaseTime(long dbTime) {
		this.databaseTime = dbTime;
	}

	public Long getTimestamp() {
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
		return "TrafficGeneratorTimeData{" +
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
