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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "test_id", referencedColumnName = "id")
    @JsonBackReference
    private Test test;

    @Column(name = "application_time")
    private long applicationTime;

    @Column(name = "database_time")
    private long databaseTime;
    
    @Column(name = "timestamp")
    private long timestamp;
    
    @Column(name = "endpoint_url")
    private String endpointUrl;

    @Column(name = "method")
    private String method;
    
    public TrafficGeneratorTimeData() {
    }

	public TrafficGeneratorTimeData(int id, Test test, long appTime, long dbTime, long timestamp, String endpointUrl,
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
		this.id = 0;
		this.test = test;
		this.applicationTime = trafficGeneratorTimeDataDTO.getApplicationTime();
		this.databaseTime = trafficGeneratorTimeDataDTO.getDatabaseTime();
		this.timestamp = trafficGeneratorTimeDataDTO.getTimestamp();
		this.endpointUrl = trafficGeneratorTimeDataDTO.getEndpointUrl();
		this.method = trafficGeneratorTimeDataDTO.getMethod();
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

	public void setApplicationTime(long appTime) {
		this.applicationTime = appTime;
	}

	public long getDatabaseTime() {
		return databaseTime;
	}

	public void setDatabaseTime(long dbTime) {
		this.databaseTime = dbTime;
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
