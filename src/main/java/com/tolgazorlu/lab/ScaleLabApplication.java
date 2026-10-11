package com.tolgazorlu.lab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods
public class ScaleLabApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScaleLabApplication.class, args);
	}

}
