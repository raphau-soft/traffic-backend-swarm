package com.raphau.trafficgenerator.dto;

public class CpuDataDTO {

    private long timestamp;

    private Double cpuUsage;

    public CpuDataDTO() {
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