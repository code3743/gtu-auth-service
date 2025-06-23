package com.gtu.auth_service.presentation.exception;

import com.gtu.auth_service.application.dto.ErrorResponseDTO;
import com.gtu.auth_service.infrastructure.logs.LogPublisher;
import feign.FeignException;
import feign.Request;
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
import java.util.HashMap;
import java.util.Map;

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

        Map<String, Object> expectedDetails = new HashMap<>();
        expectedDetails.put("reason", "Invalid argument");
        expectedDetails.put("exceptionMessage", "Invalid input");

        verify(logPublisher).sendLog(anyString(), eq("auth-service"), eq("ERROR"), eq("Credenciales inválidas"), eq(expectedDetails));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().message());
        assertEquals("Unauthorized", response.getBody().error());
    }

    @Test
    void handleGeneralException_ShouldLogAndReturnInternalServerError() {
        Exception ex = new RuntimeException("Unexpected failure");

        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleGeneralException(ex);

        verify(logPublisher).sendLog(anyString(), eq("auth-service"), eq("ERROR"), eq("An unexpected error occurred"), anyMap());
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
}
