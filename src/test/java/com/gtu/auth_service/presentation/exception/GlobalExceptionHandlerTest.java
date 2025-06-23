package com.gtu.auth_service.presentation.exception;

import com.gtu.auth_service.application.dto.ErrorResponseDTO;
import com.gtu.auth_service.infrastructure.logs.LogPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    @Mock
    private LogPublisher logPublisher;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleFeignException_ShouldReturnInternalServerError_WhenStatusNull() {
        feign.FeignException ex = mock(feign.FeignException.class);
        when(ex.status()).thenReturn(-1);

        ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler.handleFeignException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().error());
    }
}