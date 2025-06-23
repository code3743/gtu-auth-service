package com.gtu.auth_service.infrastructure.logs;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class LogCheckService {

    private final LogPublisher logPublisher;

    public LogCheckService(LogPublisher logPublisher) {
        this.logPublisher = logPublisher;
    }

    public void checkRabbitMQConnection() {
        try {
            logPublisher.sendLog(
                Instant.now().toString(),
                "auth-service",
                "INFO",
                "RabbitMQ connection verified",
                Map.of()
            );
            System.out.println("✅ RabbitMQ connection is active.");
            logPublisher.processPendingLogs();
            
        } catch (Exception e) {
            System.err.println("❌ RabbitMQ connection failed: " + e.getMessage());
            logPublisher.saveLogToFile(
                Map.of(
                    "timestamp", Instant.now().toString(),
                    "service", "auth-service",
                    "level", "ERROR",
                    "message", "Failed to connect to RabbitMQ",
                    "details", Map.of("error", e.getMessage())
                )
            );
        }
    }



}