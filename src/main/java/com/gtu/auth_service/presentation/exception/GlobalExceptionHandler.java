package com.gtu.auth_service.presentation.exception;

import com.gtu.auth_service.application.dto.ErrorResponseDTO;
import com.gtu.auth_service.infrastructure.logs.LogPublisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final LogPublisher logPublisher;

    @Value("${service.name:auth-service}")
    private String serviceName;

    public GlobalExceptionHandler(LogPublisher logPublisher) {
        this.logPublisher = logPublisher;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        
        // Crear el objeto details
        Map<String, Object> details = new HashMap<>();
        details.put("reason", "Invalid argument");
        details.put("exceptionMessage", ex.getMessage());

        // Tomar la hora del evento y enviar el log
        String timestamp = Instant.now().toString();
        logPublisher.sendLog(timestamp, serviceName, "ERROR", "Credenciales inválidas", details);

        // Responder con ErrorResponseDTO
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception ex) {
        // Crear el objeto details
        Map<String, Object> details = new HashMap<>();
        details.put("reason", "Unexpected error");
        details.put("exceptionMessage", ex.getMessage());

        // Enviar el log
        String timestamp = Instant.now().toString();
        logPublisher.sendLog(timestamp, serviceName, "ERROR", "An unexpected error occurred", details);

        // Responder con ErrorResponseDTO
        ErrorResponseDTO error = new ErrorResponseDTO("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
