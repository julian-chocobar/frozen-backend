package com.enigcode.frozen_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.enigcode.frozen_backend.common.SecurityProperties;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
public class FrozenBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrozenBackendApplication.class, args);
		System.err.println("EnigCode");
	}

}
