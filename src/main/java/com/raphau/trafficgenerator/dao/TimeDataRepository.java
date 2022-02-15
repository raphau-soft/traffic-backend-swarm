package com.raphau.trafficgenerator.dao;

import com.raphau.trafficgenerator.entity.TimeData;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeDataRepository extends JpaRepository<TimeData, Integer> {
}
