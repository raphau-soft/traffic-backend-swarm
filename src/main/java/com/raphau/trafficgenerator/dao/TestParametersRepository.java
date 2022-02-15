package com.raphau.trafficgenerator.dao;

import com.raphau.trafficgenerator.entity.TestParameters;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TestParametersRepository extends JpaRepository<TestParameters, Integer> {
}
