package com.mvargas.clinical_assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class ClinicalAssistantApplication {
	static void main(String[] args) {
		SpringApplication.run(ClinicalAssistantApplication.class, args);
	}
}
