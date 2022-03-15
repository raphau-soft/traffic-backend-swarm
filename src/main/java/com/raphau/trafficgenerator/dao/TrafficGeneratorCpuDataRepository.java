package com.raphau.trafficgenerator.dao;

import com.raphau.trafficgenerator.entity.TrafficGeneratorCpuData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrafficGeneratorCpuDataRepository extends JpaRepository<TrafficGeneratorCpuData, Integer> {
}
