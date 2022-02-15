package com.raphau.trafficgenerator.entity;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.io.Serializable;

@Entity
@Table(name="time_data", schema="traffic_generator")
public class TimeData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "test_id", referencedColumnName = "id")
    @JsonBackReference
    private Test test;

    @Column(name = "api_time")
    private long apiTime;

    @Column(name = "application_time")
    private long appTime;

    @Column(name = "database_time")
    private Double dbTime;
    
    @Column(name = "timestamp")
    private long timestamp;
    
    @Column(name = "endpoint_url")
    private String endpointUrl;

    @Column(name = "method")
    private String method;
    
    public TimeData() {
    }

	public TimeData(int id, Test test, long apiTime, long appTime, Double dbTime, long timestamp, String endpointUrl,
			String method) {
		super();
		this.id = id;
		this.test = test;
		this.apiTime = apiTime;
		this.appTime = appTime;
		this.dbTime = dbTime;
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

	public long getApiTime() {
		return apiTime;
	}

	public void setApiTime(long apiTime) {
		this.apiTime = apiTime;
	}

	public long getAppTime() {
		return appTime;
	}

	public void setAppTime(long appTime) {
		this.appTime = appTime;
	}

	public Double getDbTime() {
		return dbTime;
	}

	public void setDbTime(Double dbTime) {
		this.dbTime = dbTime;
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


}
