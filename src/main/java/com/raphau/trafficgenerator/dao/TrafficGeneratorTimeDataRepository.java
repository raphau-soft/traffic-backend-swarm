package com.raphau.trafficgenerator.dao;

import com.raphau.trafficgenerator.entity.Test;
import com.raphau.trafficgenerator.entity.TrafficGeneratorTimeData;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrafficGeneratorTimeDataRepository extends JpaRepository<TrafficGeneratorTimeData, Integer> {
    Page<TrafficGeneratorTimeData> findAllByTest(Pageable pageable, Test test);

    @Query("SELECT DISTINCT endpointUrl FROM TrafficGeneratorTimeData WHERE test = ?1")
    List<String> findDistinctUrlByTestId(Test test);


}
