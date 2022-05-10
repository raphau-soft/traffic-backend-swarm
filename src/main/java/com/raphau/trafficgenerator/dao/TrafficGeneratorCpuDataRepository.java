package com.raphau.trafficgenerator.dao;

import com.raphau.trafficgenerator.entity.Test;
import com.raphau.trafficgenerator.entity.TrafficGeneratorCpuData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrafficGeneratorCpuDataRepository extends JpaRepository<TrafficGeneratorCpuData, Integer> {
    Page<TrafficGeneratorCpuData> findAllByTest(Pageable pageable, Test test);
}
