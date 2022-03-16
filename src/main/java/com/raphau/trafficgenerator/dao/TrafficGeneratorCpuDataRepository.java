package com.raphau.trafficgenerator.dao;

import com.raphau.trafficgenerator.entity.Test;
import com.raphau.trafficgenerator.entity.TrafficGeneratorCpuData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrafficGeneratorCpuDataRepository extends JpaRepository<TrafficGeneratorCpuData, Integer> {
    List<TrafficGeneratorCpuData> findAllByTest(Test test);
}
