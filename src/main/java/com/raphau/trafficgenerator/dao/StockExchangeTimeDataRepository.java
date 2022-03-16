package com.raphau.trafficgenerator.dao;

import com.raphau.trafficgenerator.entity.StockExchangeTimeData;
import com.raphau.trafficgenerator.entity.Test;
import com.raphau.trafficgenerator.entity.TrafficGeneratorTimeData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockExchangeTimeDataRepository extends JpaRepository<StockExchangeTimeData, Integer> {
    List<StockExchangeTimeData> findAllByTest(Test test);
}
