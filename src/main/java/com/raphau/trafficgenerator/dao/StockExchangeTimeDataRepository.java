package com.raphau.trafficgenerator.dao;

import com.raphau.trafficgenerator.entity.StockExchangeTimeData;
import com.raphau.trafficgenerator.entity.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockExchangeTimeDataRepository extends JpaRepository<StockExchangeTimeData, Integer> {
    Page<StockExchangeTimeData> findAllByTest(Pageable pageable, Test test);
}
