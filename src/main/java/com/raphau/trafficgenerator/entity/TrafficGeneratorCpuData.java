package com.raphau.trafficgenerator.entity;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name="traffic_generator_cpu_data", schema="traffic_generator")
public class TrafficGeneratorCpuData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "test_id", referencedColumnName = "id")
    @JsonBackReference
    private Test test;

    @Column(name = "timestamp")
    private long timestamp;

    @Column(name = "cpu_usage")
    private Double cpuUsage;

    @Column(name = "memory")
    private Double memory;

    public TrafficGeneratorCpuData() {
    }

	public TrafficGeneratorCpuData(int id, Test test, long timestamp, Double cpuUsage, Double memory) {
		this.id = id;
		this.test = test;
		this.timestamp = timestamp;
		this.cpuUsage = cpuUsage;
		this.memory = memory;
	}

	public Double getMemory() {
		return memory;
	}

	public void setMemory(Double memory) {
		this.memory = memory;
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



	public long getTimestamp() {
		return timestamp;
	}



	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}



	public Double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }


}
