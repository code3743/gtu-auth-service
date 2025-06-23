package com.gtu.auth_service.application.service;

import com.gtu.auth_service.application.dto.LoginRequestDTO;
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
}