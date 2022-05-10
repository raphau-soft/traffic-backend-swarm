package com.raphau.trafficgenerator.dto;

import javax.persistence.Column;

public class CpuDataDTO {

    private long timestamp;

    private Double cpuUsage;

	private Double memory;

	private String stockId;

	public CpuDataDTO(long timestamp, Double cpuUsage, Double memory, String stockId) {
		this.timestamp = timestamp;
		this.cpuUsage = cpuUsage;
		this.memory = memory;
		this.stockId = stockId;
	}

	public CpuDataDTO() {
    }

	public Double getMemory() {
		return memory;
	}

	public void setMemory(Double memory) {
		this.memory = memory;
	}

	public String getStockId() {
		return stockId;
	}

	public void setStockId(String stockId) {
		this.stockId = stockId;
	}

	public CpuDataDTO(long timestamp, Double cpuUsage) {
		super();
		this.timestamp = timestamp;
		this.cpuUsage = cpuUsage;
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
		return "CpuData [timestamp=" + timestamp + ", cpuUsage=" + cpuUsage + "]";
	}


}