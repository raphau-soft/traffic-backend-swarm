package com.raphau.trafficgenerator.dao;

import com.raphau.trafficgenerator.entity.StockExchangeCpuData;
import com.raphau.trafficgenerator.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockExchangeCpuDataRepository extends JpaRepository<StockExchangeCpuData, Integer> {
    List<StockExchangeCpuData> findAllByTest(Test test);
}
