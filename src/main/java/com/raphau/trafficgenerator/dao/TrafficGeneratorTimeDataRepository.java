package com.raphau.trafficgenerator.dao;

import com.raphau.trafficgenerator.entity.TrafficGeneratorTimeData;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrafficGeneratorTimeDataRepository extends JpaRepository<TrafficGeneratorTimeData, Integer> {
}
