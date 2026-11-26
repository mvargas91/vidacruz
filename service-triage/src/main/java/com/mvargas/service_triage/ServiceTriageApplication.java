package com.mvargas.service_triage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServiceTriageApplication {

	static void main(String[] args) {
		SpringApplication.run(ServiceTriageApplication.class, args);
	}

}
