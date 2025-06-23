package com.gtu.auth_service.application.service;

import com.gtu.auth_service.application.dto.LoginRequestDTO;
import com.gtu.auth_service.application.dto.RegisterRequestDTO;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void findUserByEmail_WhenNoUserDoesNotExist_ShouldReturnNull() {
        when(userClient.getUserByEmail("nonexistent@example.com")).thenReturn(null);
        when(passengerClient.getPassengerByEmail("nonexistent@example.com")).thenReturn(null);

        AuthUser result = authService.findUserByEmail("nonexistent@example.com");

        assertNull(result);
    }

    @Test
    void mapToRole_WhenRoleIsSuperAdmin_ShouldReturnSuperAdmin() {
        assertEquals(Role.SUPERADMIN, authService.mapToRole("SUPERADMIN"));
    }

    @Test
    void mapToRole_WhenRoleIsAdmin_ShouldReturnAdmin() {
        assertEquals(Role.ADMIN, authService.mapToRole("ADMIN"));
    }

    @Test
    void mapToRole_WhenRoleIsDriver_ShouldReturnDriver() {
        assertEquals(Role.DRIVER, authService.mapToRole("DRIVER"));
    }

    @Test
    void mapToRole_WhenRoleIsNull_ShouldThrowException() {
        IllegalArgumentException exception = 
            assertThrows(IllegalArgumentException.class, () -> authService.mapToRole(null));
        assertEquals("Role cannot be null", exception.getMessage());
    }

    @Test
    void mapToRole_WhenRoleIsInvalid_ShouldThrowException() {
        IllegalArgumentException exception = 
            assertThrows(IllegalArgumentException.class, () -> authService.mapToRole("INVALID"));
        assertEquals("Invalid role: INVALID", exception.getMessage());
    }

    @Test
    void authenticate_ShouldThrowUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, 
            () -> authService.authenticate(new LoginRequestDTO("email", "pass")));
    }

    @Test
    void findPassengerByEmail_WhenPassengerExists_ShouldReturnAuthUser() {
        UserServiceResponse passengerResponse = new UserServiceResponse(2L, "Jane Doe", "jane@example.com", "encodedPass", null);
        when(passengerClient.getPassengerByEmail("jane@example.com")).thenReturn(passengerResponse);

        AuthUser result = authService.findPassengerByEmail("jane@example.com");

        assertEquals(2L, result.id());
        assertEquals("Jane Doe", result.name());
        assertEquals("jane@example.com", result.email());
        assertEquals("encodedPass", result.password());
        assertEquals(Role.PASSENGER, result.role());
    }

    @Test
    void registerPassenger_ShouldReturnAuthUser_WhenRegistrationSucceeds() {
        RegisterRequestDTO request = new RegisterRequestDTO("Jane Doe", "jane@example.com", "pass123");
        UserServiceResponse response = new UserServiceResponse(null, "Jane Doe", "jane@example.com", "pass123", null);
        when(passengerClient.registerPassenger(request)).thenReturn(response);

        AuthUser result = authService.registerPassenger("Jane Doe", "jane@example.com", "pass123");

        assertEquals("Jane Doe", result.name());
        assertEquals("jane@example.com", result.email());
        assertEquals("pass123", result.password());
        assertEquals(Role.PASSENGER, result.role());
    }

    @Test
    void findPassengerByEmail_WhenPassengerNotFound_ShouldReturnNull() {
        when(passengerClient.getPassengerByEmail("nonexistent@example.com")).thenReturn(null);

        AuthUser result = authService.findPassengerByEmail("nonexistent@example.com");

        assertNull(result);
    }
}