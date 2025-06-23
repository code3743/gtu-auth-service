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

    public void checkRabbitMQConnection() throws InterruptedException {  
        logPublisher.sendLog(
            Instant.now().toString(),
            "auth-service",
            "INFO",
            "RabbitMQ connection verified",
            Map.of()
        );  
    }
}