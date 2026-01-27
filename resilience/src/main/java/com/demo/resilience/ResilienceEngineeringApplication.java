package com.demo.resilience;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ResilienceEngineeringApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResilienceEngineeringApplication.class, args);
	}

}
