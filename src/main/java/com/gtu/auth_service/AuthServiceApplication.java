package com.gtu.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

import com.gtu.auth_service.infrastructure.logs.LogCheckService;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AuthServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceApplication.class);

    public static void main(String[] args) {
        try {
            SpringApplication.run(AuthServiceApplication.class, args);
            logger.info("Auth Service started successfully");
        } catch (Exception e) {
            logger.error("[WARN] Failed to start Auth Service", e);
        }
    }

    @Bean
	public CommandLineRunner checkInfrastructure(LogCheckService healthCheckService) {
		return args -> healthCheckService.checkRabbitMQConnection();
	}
}