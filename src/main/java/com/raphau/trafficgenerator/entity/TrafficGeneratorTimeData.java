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

	@Column(name = "api_time")
	private Long apiTime;

    @Column(name = "application_time")
    private Long applicationTime;

    @Column(name = "database_time")
    private Long databaseTime;
    
    @Column(name = "timestamp")
    private Long timestamp;

    @Column(name = "queue_size_forward")
    private int queueSizeForward;

    @Column(name = "queue_size_back")
    private int queueSizeBack;
    
    @Column(name = "endpoint_url")
    private String endpointUrl;

	@Column(name = "stock_id")
	private String stockId;
    
    public TrafficGeneratorTimeData() {
    }

	public int getQueueSizeForward() {
		return queueSizeForward;
	}

	public void setQueueSizeForward(int queueSizeForward) {
		this.queueSizeForward = queueSizeForward;
	}

	public int getQueueSizeBack() {
		return queueSizeBack;
	}

	public void setQueueSizeBack(int queueSizeBack) {
		this.queueSizeBack = queueSizeBack;
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

	public TrafficGeneratorTimeData(int id, Test test, Long apiTime, Long applicationTime, Long databaseTime, Long timestamp, int queueSizeForward, String endpointUrl) {
		this.id = id;
		this.test = test;
		this.apiTime = apiTime;
		this.applicationTime = applicationTime;
		this.databaseTime = databaseTime;
		this.timestamp = timestamp;
		this.queueSizeForward = queueSizeForward;
		this.endpointUrl = endpointUrl;
	}

	public void updateWithDTO(TrafficGeneratorTimeData trafficGeneratorTimeData, TrafficGeneratorTimeDataDTO trafficGeneratorTimeDataDTO, Test test) {
		this.id = trafficGeneratorTimeDataDTO.getId();
		this.test = test;
		this.apiTime = System.currentTimeMillis() - trafficGeneratorTimeData.getApiTime();
		this.applicationTime = trafficGeneratorTimeDataDTO.getApplicationTime();
		this.databaseTime = trafficGeneratorTimeDataDTO.getDatabaseTime();
		this.timestamp = trafficGeneratorTimeDataDTO.getTimestamp();
		this.endpointUrl = trafficGeneratorTimeData.getEndpointUrl();
		this.stockId = trafficGeneratorTimeDataDTO.getStockId();
		this.queueSizeBack = trafficGeneratorTimeDataDTO.getQueueSizeBack();
		this.queueSizeForward = trafficGeneratorTimeData.getQueueSizeForward();
	}

	public Long getApiTime() {
		return apiTime;
	}

	public void setApiTime(Long apiTime) {
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

	@Override
	public String toString() {
		return "TrafficGeneratorTimeData{" +
				"id=" + id +
				", test=" + test +
				", appTime=" + applicationTime +
				", dbTime=" + databaseTime +
				", timestamp=" + timestamp +
				", endpointUrl='" + endpointUrl + '\'' +
				'}';
	}
}
