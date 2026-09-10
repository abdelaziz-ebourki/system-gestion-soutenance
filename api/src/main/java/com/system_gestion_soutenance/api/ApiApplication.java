package com.system_gestion_soutenance.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SuppressWarnings("PMD")

@SpringBootApplication
public class ApiApplication {

	private ApiApplication() {
	}

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}
}