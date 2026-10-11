package com.tolgazorlu.lab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScaleLabApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScaleLabApplication.class, args);
	}

}
