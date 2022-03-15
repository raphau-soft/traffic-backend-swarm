package com.raphau.trafficgenerator.dao;

import com.raphau.trafficgenerator.entity.StockExchangeCpuData;
import com.raphau.trafficgenerator.entity.TrafficGeneratorCpuData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockExchangeCpuDataRepository extends JpaRepository<StockExchangeCpuData, Integer> {
}
