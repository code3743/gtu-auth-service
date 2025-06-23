package com.gtu.auth_service.infrastructure.logs;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
public class LogPublisher {

    private final AmqpTemplate amqpTemplate;

    @Value("${rabbitmq.exchange.log}")
    private String exchange;

    @Value("${rabbitmq.routingkey.log}")
    private String routingKey;

    private final ObjectMapper objectMapper;

    private final String pendingLogsFile = "pending_logs.json";

    public LogPublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void sendLog(String timestamp, String service, String level, String message, Map<String, Object> details) {
        try {
            Map<String, Object> log = Map.of(
                "timestamp", timestamp,
                "service", service,
                "level", level,
                "message", message,
                "details", details
            );

            String logJson = objectMapper.writeValueAsString(log);
            amqpTemplate.convertAndSend(exchange, routingKey, logJson);

            System.out.println("✅ LOG SENT: " + logJson);
        } catch (Exception e) {
            System.err.println("❌ ERROR SENDING LOG: " + e.getMessage());
            saveLogToFile(Map.of(
                "timestamp", timestamp,
                "service", service,
                "level", level,
                "message", message,
                "details", details
            ));
        }
    }

    public void processPendingLogs() {
        List<Map<String, Object>> pendingLogs = readLogsFromFile();

        Iterator<Map<String, Object>> iterator = pendingLogs.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> log = iterator.next();
            try {
                String logJson = objectMapper.writeValueAsString(log);
                amqpTemplate.convertAndSend(exchange, routingKey, logJson);
                System.out.println("✅ PENDING LOG SENT: " + logJson);
                iterator.remove(); 
            } catch (Exception e) {
                System.err.println("❌ ERROR SENDING PENDING LOG: " + e.getMessage());
                break; 
            }
        }
        writeLogsToFile(pendingLogs);
    }

    public void saveLogToFile(Map<String, Object> log) {
        System.out.println("📝 SAVING LOG TO FILE: " + log);
        List<Map<String, Object>> pendingLogs = readLogsFromFile();
        pendingLogs.add(log);
        writeLogsToFile(pendingLogs);
    }

    private List<Map<String, Object>> readLogsFromFile() {
        try {
            File file = new File(pendingLogsFile);
            if (!file.exists()) {
                System.out.println("📂 PENDING LOGS FILE NOT FOUND, CREATING NEW ONE.");
                return new ArrayList<>();
            }
            String content = new String(Files.readAllBytes(Paths.get(pendingLogsFile)));
            System.out.println("📂 READING PENDING LOGS FILE: " + pendingLogsFile);
            return objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
        } catch (IOException e) {
            System.err.println("❌ ERROR READING PENDING LOGS FILE: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void writeLogsToFile(List<Map<String, Object>> logs) {
        try {
            objectMapper.writeValue(new File(pendingLogsFile), logs);
        } catch (IOException e) {
            System.err.println("❌ ERROR WRITING TO PENDING LOGS FILE: " + e.getMessage());
        }
    }
}