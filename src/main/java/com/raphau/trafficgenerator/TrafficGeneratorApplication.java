package com.raphau.trafficgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
public class TrafficGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrafficGeneratorApplication.class, args);
	}

}
