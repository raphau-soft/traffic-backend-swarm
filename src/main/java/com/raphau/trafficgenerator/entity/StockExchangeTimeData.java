package com.raphau.trafficgenerator.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.raphau.trafficgenerator.dto.TimeDataDTO;

import javax.persistence.*;

@Entity
@Table(name="stock_exchange_time_data", schema = "stock_exchange")
public class StockExchangeTimeData {

	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 @Column(name="id")
	 private int id;

	 @ManyToOne(targetEntity = Test.class, fetch = FetchType.LAZY, optional = false)
	 @JoinColumn(name="test_id", nullable = false)
	 @JsonBackReference
	 private Test test;	
	 
	 @Column(name="timestamp")
	 private long timestamp;
	 
	 @Column(name="database_time")
	 private long databaseTime;

	 @Column(name = "application_time")
	 private long applicationTime;

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

	public StockExchangeTimeData() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (applicationTime ^ (applicationTime >>> 32));
		result = prime * result + (int) (databaseTime ^ (databaseTime >>> 32));
		result = prime * result + id;
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StockExchangeTimeData other = (StockExchangeTimeData) obj;
		if (applicationTime != other.applicationTime)
			return false;
		if (databaseTime != other.databaseTime)
			return false;
		if (id != other.id)
			return false;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TimeData [id=" + id + ", test=" + test + ", timestamp=" + timestamp + ", databaseTime=" + databaseTime
				+ ", applicationTime=" + applicationTime + ", semaphoreWaitTime=" + "]";
	}

	public StockExchangeTimeData(int id, Test test, long timestamp, long databaseTime, long applicationTime) {
		super();
		this.id = id;
		this.test = test;
		this.timestamp = timestamp;
		this.databaseTime = databaseTime;
		this.applicationTime = applicationTime;
	}

	public StockExchangeTimeData(TimeDataDTO timeDataDTO, Test test) {
		super();
		this.id = 0;
		this.test = test;
		this.timestamp = timeDataDTO.getTimestamp();
		this.databaseTime = timeDataDTO.getDatabaseTime();
		this.applicationTime = timeDataDTO.getApplicationTime();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getDatabaseTime() {
		return databaseTime;
	}

	public void setDatabaseTime(long databaseTime) {
		this.databaseTime = databaseTime;
	}

	public long getApplicationTime() {
		return applicationTime;
	}

	public void setApplicationTime(long applicationTime) {
		this.applicationTime = applicationTime;
	}
	 
}