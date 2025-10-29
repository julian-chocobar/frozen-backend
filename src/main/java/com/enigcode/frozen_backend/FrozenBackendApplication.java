package com.enigcode.frozen_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.enigcode.frozen_backend.common.SecurityProperties;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
@EnableScheduling
public class FrozenBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrozenBackendApplication.class, args);
		System.err.println("EnigCode");
	}

}
