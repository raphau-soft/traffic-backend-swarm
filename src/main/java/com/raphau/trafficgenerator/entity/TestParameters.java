package com.raphau.trafficgenerator.entity;

import javax.persistence.*;

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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "test_id", referencedColumnName = "id")
    @JsonBackReference
    private Test test;

    @Column(name = "number_of_requests")
    private int number_of_requests;

    @Column(name = "number_of_users")
    private int number_of_users;

    @Column(name = "test_time")
    private int test_time;
    
    @Column(name = "time_between_requests")
    private int time_between_requests;
    
    
    public TestParameters() {
    }

    public TestParameters(RunTestDTO runTestDTO) {
    	this.number_of_requests = runTestDTO.getRequestsNumber();
    	this.number_of_users = runTestDTO.getNumberOfUsers();
    	this.test_time = runTestDTO.getTestTime();
    	this.time_between_requests = runTestDTO.getTimeBetweenRequests();
    }

	public TestParameters(int id, Test test, int number_of_requests, int number_of_users, int test_time,
			int time_between_requests) {
		super();
		this.id = id;
		this.test = test;
		this.number_of_requests = number_of_requests;
		this.number_of_users = number_of_users;
		this.test_time = test_time;
		this.time_between_requests = time_between_requests;
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


	public int getNumber_of_users() {
		return number_of_users;
	}


	public void setNumber_of_users(int number_of_users) {
		this.number_of_users = number_of_users;
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
				", number_of_users=" + number_of_users +
				", test_time=" + test_time +
				", time_between_requests=" + time_between_requests +
				'}';
	}
}
