package com.raphau.trafficgenerator.dao;

import com.raphau.trafficgenerator.entity.CpuData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CpuDataRepository extends JpaRepository<CpuData, Integer> {
}
