package com.gtu.auth_service.application.service;

import com.gtu.auth_service.domain.model.AuthUser;
import com.gtu.auth_service.domain.model.Role;
import com.gtu.auth_service.infrastructure.client.PassengerClient;
import com.gtu.auth_service.infrastructure.client.UserClient;
import com.gtu.auth_service.infrastructure.client.dto.UserServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {
    @Mock
    private UserClient userClient;

    @Mock
    private PassengerClient passengerClient;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findUserByEmail_WhenUserExists_ShouldReturnAuthUser() {
        UserServiceResponse userResponse = new UserServiceResponse(1L, "John Doe", "jhon.doe@example.com", "encodedPass", "DRIVER");
        when(userClient.getUserByEmail("jhon.doe@example.com")).thenReturn(userResponse);

        AuthUser result = authService.findUserByEmail("jhon.doe@example.com");

        assertEquals(1L, result.id());
        assertEquals("John Doe", result.name());
        assertEquals("jhon.doe@example.com", result.email());
        assertEquals("encodedPass", result.password());
        assertEquals(Role.DRIVER, result.role());
    }

    @Test
    void findUserByEmail_WhenNoUserOrPassengerExists_ShouldReturnNull() {
        when(userClient.getUserByEmail("nonexistent@example.com")).thenReturn(null);
        when(passengerClient.getPassengerByEmail("nonexistent@example.com")).thenReturn(null);

        AuthUser result = authService.findUserByEmail("nonexistent@example.com");

        assertNull(result);
    }

    @Test
    void mapToRole_WhenValidRole_ShouldMapCorrectly() {
        assertEquals(Role.SUPERADMIN, authService.mapToRole("SUPERADMIN"));
        assertEquals(Role.ADMIN, authService.mapToRole("ADMIN"));
        assertEquals(Role.DRIVER, authService.mapToRole("DRIVER"));
    }

    @Test
    void mapToRole_WhenNullRole_ShouldThrowIllegalArgumentException() {
        try {
            authService.mapToRole(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Role cannot be null", e.getMessage());
        }
    }

    @Test
    void mapToRole_WhenInvalidRole_ShouldThrowIllegalArgumentException() {
        try {
            authService.mapToRole("INVALID");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid role: INVALID", e.getMessage());
        }
    } 
}