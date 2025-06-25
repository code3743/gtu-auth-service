package com.gtu.auth_service.presentation.exception;

import com.gtu.auth_service.application.dto.ErrorResponseDTO;
import com.gtu.auth_service.domain.exception.GeneralException;
import com.gtu.auth_service.infrastructure.logs.LogPublisher;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Value("${service.name:auth-service}")
    private String serviceName;

    private final LogPublisher logPublisher;

    private static final String LOG_KEY_ERROR = "error";

    public GlobalExceptionHandler(LogPublisher logPublisher) {
        this.logPublisher = logPublisher;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {

        String message = ex.getMessage().toLowerCase();
        if (!(message.contains("user") || message.contains("role") || message.contains("not found") || 
              message.contains("invalid password") || message.contains("pending") || message.contains("expired"))) {
            logPublisher.sendLog(Instant.now().toString(), 
            serviceName, 
            "WARN", 
            "Validation Error", 
            Map.of(LOG_KEY_ERROR, ex.getMessage()));
        }

        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception ex) {
        String severity = "ERROR";
        if (ex instanceof NullPointerException || ex instanceof IllegalStateException) {
            severity = "CRITICAL";
        }

        logPublisher.sendLog(Instant.now().toString(), 
                            serviceName, 
                            severity, 
                            "Unexpected error occurred",
                            Map.of(LOG_KEY_ERROR, ex.getMessage()));

        ErrorResponseDTO error = new ErrorResponseDTO("An unexpected error occurred" , HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponseDTO> handleFeignException(FeignException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        logPublisher.sendLog(Instant.now().toString(), 
                            serviceName, "ERROR", 
                            "External service communication failed", 
                            Map.of("status", status.toString(), LOG_KEY_ERROR, ex.getMessage()));

        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), status.value(), status.getReasonPhrase());
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorResponseDTO> handleFeignNotFound(FeignException.NotFound ex) {
        ErrorResponseDTO error = new ErrorResponseDTO("User not found in remote service", HttpStatus.NOT_FOUND.value(), "Not Found");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpException(GeneralException ex, HttpServletRequest request) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                ex.getMessage(),
                ex.getStatusCode(),
                HttpStatus.valueOf(ex.getStatusCode()).getReasonPhrase()
        );
        return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getStatusCode()));
    }

}
