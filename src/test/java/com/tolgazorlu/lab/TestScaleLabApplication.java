package com.tolgazorlu.lab;

import org.springframework.boot.SpringApplication;

public class TestScaleLabApplication {

	public static void main(String[] args) {
		SpringApplication.from(ScaleLabApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
