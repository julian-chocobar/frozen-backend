package com.enigcode.frozen_backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.enigcode.frozen_backend.common.SecurityProperties;
import com.enigcode.frozen_backend.common.service.DataLoaderService;
import lombok.RequiredArgsConstructor;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
@EnableScheduling
@RequiredArgsConstructor
public class FrozenBackendApplication implements CommandLineRunner {

	private final DataLoaderService dataLoaderService;

	public static void main(String[] args) {
		SpringApplication.run(FrozenBackendApplication.class, args);
		System.err.println("EnigCode");
	}

	@Override
	public void run(String... args) throws Exception {
		dataLoaderService.loadSampleDataIfEmpty();
	}

}
