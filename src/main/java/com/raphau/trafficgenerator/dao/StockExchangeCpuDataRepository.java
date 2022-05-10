package com.raphau.trafficgenerator.dao;

import com.raphau.trafficgenerator.entity.StockExchangeCpuData;
import com.raphau.trafficgenerator.entity.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockExchangeCpuDataRepository extends JpaRepository<StockExchangeCpuData, Integer> {
    Page<StockExchangeCpuData> findAllByTest(Pageable pageable, Test test);
}
