package com.gathix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GathixApplication {

	public static void main(String[] args) {
		SpringApplication.run(GathixApplication.class, args);
	}

}
