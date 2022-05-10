package com.raphau.trafficgenerator.entity;

import javax.persistence.*;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.raphau.trafficgenerator.dto.RunTestDTO;

import java.io.Serializable;

@Entity
@Table(name="test_parameters", schema="traffic_generator")
public class TestParameters {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "test_id", referencedColumnName = "id")
    @JsonBackReference
    private Test test;

    @Column(name = "number_of_requests")
    private int number_of_requests;

    @Column(name = "test_time")
    private int test_time;
    
    @Column(name = "time_between_requests")
    private int time_between_requests;

	@Column(name = "first")
	private int first;
	@Column(name = "second")
	private int second;
	@Column(name = "third")
	private int third;
	@Column(name = "request_limit")
	private boolean requestLimit;
	@Column(name = "time_limit")
	private boolean timeLimit;
	@Column(name = "break_between_trades")
	private int breakBetweenTrades;
    
    
    public TestParameters() {
    }

    public TestParameters(RunTestDTO runTestDTO) {
    	this.number_of_requests = runTestDTO.getRequestsNumber();
    	this.test_time = runTestDTO.getTestTime();
    	this.time_between_requests = runTestDTO.getTimeBetweenRequests();
    	this.first = runTestDTO.getFirst();
    	this.second = runTestDTO.getSecond();
    	this.third = runTestDTO.getThird();
    	this.requestLimit = runTestDTO.isRequestLimit();
    	this.timeLimit = runTestDTO.isTimeLimit();
    }

	public TestParameters(int id, Test test, int number_of_requests, int number_of_users, int test_time,
			int time_between_requests) {
		super();
		this.id = id;
		this.test = test;
		this.number_of_requests = number_of_requests;
		this.test_time = test_time;
		this.time_between_requests = time_between_requests;
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


	public int getNumber_of_requests() {
		return number_of_requests;
	}


	public void setNumber_of_requests(int number_of_requests) {
		this.number_of_requests = number_of_requests;
	}

	public int getTest_time() {
		return test_time;
	}


	public void setTest_time(int test_time) {
		this.test_time = test_time;
	}


	public int getTime_between_requests() {
		return time_between_requests;
	}


	public void setTime_between_requests(int time_between_requests) {
		this.time_between_requests = time_between_requests;
	}

	@Override
	public String toString() {
		return "TestParameters{" +
				"id=" + id +
				", test=" + test +
				", number_of_requests=" + number_of_requests +
				", test_time=" + test_time +
				", time_between_requests=" + time_between_requests +
				'}';
	}
}
