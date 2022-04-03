package com.raphau.trafficgenerator.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.raphau.trafficgenerator.dto.TimeDataDTO;

import javax.persistence.*;
import java.util.Objects;

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

	 @Column(name = "number_of_sell_offers")
	 private long numberOfSellOffers;

	 @Column(name = "number_of_buy_offers")
	 private long numberOfBuyOffers;

	@Column(name = "stock_id")
	private String stockId;

	public StockExchangeTimeData(int id, Test test, long timestamp, long databaseTime, long applicationTime, long numberOfSellOffers, long numberOfBuyOffers, String stockId) {
		this.id = id;
		this.test = test;
		this.timestamp = timestamp;
		this.databaseTime = databaseTime;
		this.applicationTime = applicationTime;
		this.numberOfSellOffers = numberOfSellOffers;
		this.numberOfBuyOffers = numberOfBuyOffers;
		this.stockId = stockId;
	}

	public String getStockId() {
		return stockId;
	}

	public void setStockId(String stockId) {
		this.stockId = stockId;
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

	public StockExchangeTimeData() {
		super();
	}

	public long getNumberOfSellOffers() {
		return numberOfSellOffers;
	}

	public void setNumberOfSellOffers(long numberOfSellOffers) {
		this.numberOfSellOffers = numberOfSellOffers;
	}

	public long getNumberOfBuyOffers() {
		return numberOfBuyOffers;
	}

	public void setNumberOfBuyOffers(long numberOfBuyOffers) {
		this.numberOfBuyOffers = numberOfBuyOffers;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StockExchangeTimeData that = (StockExchangeTimeData) o;
		return id == that.id && timestamp == that.timestamp && databaseTime == that.databaseTime && applicationTime == that.applicationTime && numberOfSellOffers == that.numberOfSellOffers && numberOfBuyOffers == that.numberOfBuyOffers && Objects.equals(test, that.test);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, test, timestamp, databaseTime, applicationTime, numberOfSellOffers, numberOfBuyOffers);
	}

	@Override
	public String toString() {
		return "StockExchangeTimeData{" +
				"id=" + id +
				", timestamp=" + timestamp +
				", databaseTime=" + databaseTime +
				", applicationTime=" + applicationTime +
				", numberOfSellOffers=" + numberOfSellOffers +
				", numberOfBuyOffers=" + numberOfBuyOffers +
				'}';
	}

	public StockExchangeTimeData(int id, Test test, long timestamp, long databaseTime, long applicationTime) {
		super();
		this.id = id;
		this.test = test;
		this.timestamp = timestamp;
		this.databaseTime = databaseTime;
		this.applicationTime = applicationTime;
	}

	public StockExchangeTimeData(int id, Test test, long timestamp, long databaseTime, long applicationTime, long numberOfSellOffers, long numberOfBuyOffers) {
		this.id = id;
		this.test = test;
		this.timestamp = timestamp;
		this.databaseTime = databaseTime;
		this.applicationTime = applicationTime;
		this.numberOfSellOffers = numberOfSellOffers;
		this.numberOfBuyOffers = numberOfBuyOffers;
	}

	public StockExchangeTimeData(TimeDataDTO timeDataDTO, Test test) {
		super();
		this.id = 0;
		this.test = test;
		this.timestamp = timeDataDTO.getTimestamp();
		this.databaseTime = timeDataDTO.getDatabaseTime();
		this.applicationTime = timeDataDTO.getApplicationTime();
		this.numberOfBuyOffers = timeDataDTO.getNumberOfBuyOffers();
		this.numberOfSellOffers = timeDataDTO.getNumberOfSellOffers();
		this.stockId = timeDataDTO.getStockId();
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