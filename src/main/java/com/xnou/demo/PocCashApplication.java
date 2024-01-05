package com.xnou.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PocCashApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocCashApplication.class, args);
	}

}
