package com.raphau.trafficgenerator.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.raphau.trafficgenerator.dto.CpuDataDTO;

import javax.persistence.*;

@Entity
@Table(name="stock_exchange_cpu_data", schema="stock_exchange")
public class StockExchangeCpuData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @ManyToOne(targetEntity = Test.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name="test_id", nullable = false)
    @JsonBackReference
	private Test test;

    @Column(name = "timestamp")
    private long timestamp;

    @Column(name = "cpu_usage")
    private Double cpuUsage;

	@Column(name = "memory")
	private Double memory;

	@Column(name = "stock_id")
	private String stockId;

    public StockExchangeCpuData() {
    }

	public String getStockId() {
		return stockId;
	}

	public void setStockId(String stockId) {
		this.stockId = stockId;
	}

	public StockExchangeCpuData(int id, Test test, long timestamp, Double cpuUsage, Double memory, String stockId) {
		this.id = id;
		this.test = test;
		this.timestamp = timestamp;
		this.cpuUsage = cpuUsage;
		this.memory = memory;
		this.stockId = stockId;
	}

	public StockExchangeCpuData(CpuDataDTO cpuDataDTO, Test test) {
		this.id = 0;
		this.test = test;
		this.timestamp = cpuDataDTO.getTimestamp();
		this.memory = cpuDataDTO.getMemory();
		this.cpuUsage = cpuDataDTO.getCpuUsage();
		this.stockId = cpuDataDTO.getStockId();
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

	@Override
	public String toString() {
		return "CpuData [id=" + id + ", test=" + test + ", timestamp=" + timestamp + ", cpuUsage=" + cpuUsage + "]";
	}


}