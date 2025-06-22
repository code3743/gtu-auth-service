package com.gtu.auth_service.presentation.exception;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class LogPublisher {

    private final AmqpTemplate amqpTemplate;

    @Value("${rabbitmq.exchange.log}")
    private String exchange;

    @Value("${rabbitmq.routingkey.log}")
    private String routingKey;

    private final ObjectMapper objectMapper;

    public LogPublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
        this.objectMapper = new ObjectMapper(); // Para convertir objetos a JSON
    }

    public void sendLog(String timestamp, String service, String level, String message, Map<String, Object> details) {
        try {
            // Crear el objeto log
            Map<String, Object> log = Map.of(
                "timestamp", timestamp,
                "service", service,
                "level", level,
                "message", message,
                "details", details
            );

            // Convertir el log a JSON
            String logJson = objectMapper.writeValueAsString(log);

            // Enviar el log al exchange de RabbitMQ
            amqpTemplate.convertAndSend(exchange, routingKey, logJson);

            // Imprimir el log en consola para depuración
            System.out.println("🧾 Log enviado: " + logJson);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar el log: " + e.getMessage());
            e.printStackTrace();
        }
    }
}