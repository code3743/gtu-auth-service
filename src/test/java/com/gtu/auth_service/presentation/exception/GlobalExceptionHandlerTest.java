package com.gtu.auth_service.presentation.exception;

import com.gtu.auth_service.application.dto.ErrorResponseDTO;
import com.gtu.auth_service.domain.exception.GeneralException;
import com.gtu.auth_service.infrastructure.logs.LogPublisher;
import feign.FeignException;
import feign.Request;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    @Mock
    private LogPublisher logPublisher;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(globalExceptionHandler, "serviceName", "auth-service");
    }

    @Test
    void handleIllegalArgumentException_ShouldLogAndReturnUnauthorized() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleIllegalArgumentException(ex);

        verify(logPublisher, times(1)).sendLog(anyString(), eq("auth-service"), eq("WARN"), eq("Validation Error"), anyMap());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().message());
        assertEquals("Unauthorized", response.getBody().error());
    }

    @Test
    void handleGeneralException_ShouldLogAndReturnInternalServerError() {
        Exception ex = new RuntimeException("Unexpected failure");

        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleGeneralException(ex);

        verify(logPublisher, times(1)).sendLog(anyString(), eq("auth-service"), matches("ERROR|CRITICAL"), eq("Unexpected error occurred"), anyMap());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().message());
        assertEquals("Internal Server Error", response.getBody().error());
    }

    @Test
    void handleFeignException_ShouldReturnInternalServerError_WhenStatusNull() {
        FeignException feignEx = mock(FeignException.class);
        when(feignEx.status()).thenReturn(-1);
        when(feignEx.getMessage()).thenReturn("Feign failure");

        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleFeignException(feignEx);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Feign failure", response.getBody().message());
        assertEquals("Internal Server Error", response.getBody().error());
    }

    @Test
    void handleFeignException_ShouldReturnExpectedStatus_WhenStatusIsValid() {
        FeignException feignEx = mock(FeignException.class);
        when(feignEx.status()).thenReturn(400);
        when(feignEx.getMessage()).thenReturn("Bad request");

        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleFeignException(feignEx);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request", response.getBody().message());
        assertEquals("Bad Request", response.getBody().error());
    }

    @Test
    void handleFeignNotFound_ShouldReturnNotFound() {
        FeignException.NotFound notFoundEx = new FeignException.NotFound(
                "Not found",
                Request.create(Request.HttpMethod.GET, "/some-url", Collections.emptyMap(), null, StandardCharsets.UTF_8, null),
                null,
                null
        );

        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleFeignNotFound(notFoundEx);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found in remote service", response.getBody().message());
        assertEquals("Not Found", response.getBody().error());
    }

    @Test
    void handleGeneralException_ShouldReturnCorrectResponse() {
        GeneralException ex = new GeneralException("A reset token is already pending", 409);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/reset-password-request");

        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleHttpException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("A reset token is already pending", response.getBody().message());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
    }

    @Test
    void handleIllegalArgumentException_ShouldNotLog_WhenMessageContainsKnownKeywords() {
        IllegalArgumentException ex = new IllegalArgumentException("User not found");

        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleIllegalArgumentException(ex);

        verify(logPublisher, never()).sendLog(anyString(), anyString(), anyString(), anyString(), anyMap());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User not found", response.getBody().message());
    }

    @Test
    void handleGeneralException_ShouldReturnInternalServerError_WithCriticalSeverity_ForNullPointer() {
        NullPointerException ex = new NullPointerException("Something was null");

        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleGeneralException(ex);

        verify(logPublisher, times(1)).sendLog(
            anyString(),
            eq("auth-service"),
            eq("CRITICAL"),
            eq("Unexpected error occurred"),
            anyMap()
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().message());
        assertEquals("Internal Server Error", response.getBody().error());
    }

    @Test
    void handleGeneralException_ShouldReturnInternalServerError_WithCriticalSeverity_ForIllegalState() {
        IllegalStateException ex = new IllegalStateException("Invalid state");

        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleGeneralException(ex);

        verify(logPublisher, times(1)).sendLog(
            anyString(),
            eq("auth-service"),
            eq("CRITICAL"),
            eq("Unexpected error occurred"),
            anyMap()
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().message());
        assertEquals("Internal Server Error", response.getBody().error());
    }
}
