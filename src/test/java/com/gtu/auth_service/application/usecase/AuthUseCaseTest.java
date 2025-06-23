package com.gtu.auth_service.application.usecase;

import com.gtu.auth_service.application.dto.LoginRequestDTO;
import com.gtu.auth_service.application.dto.LoginResponseDTO;
import com.gtu.auth_service.application.dto.RegisterRequestDTO;
import com.gtu.auth_service.application.service.AuthServiceImpl;
import com.gtu.auth_service.application.service.JwtServiceImpl;
import com.gtu.auth_service.domain.model.AuthUser;
import com.gtu.auth_service.domain.model.Role;
import com.gtu.auth_service.domain.service.ResetPasswordService;
import com.gtu.auth_service.infrastructure.security.PasswordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AuthUseCaseTest {
    @Mock
    private AuthServiceImpl authService;

    @Mock
    private JwtServiceImpl jwtService;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private ResetPasswordService resetPasswordService;

    @InjectMocks
    private AuthUseCase authUseCase;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_ShouldReturnLoginResponse_WhenCredentialsAreValid() {
        LoginRequestDTO request = new LoginRequestDTO("john.doe@example.com", "password123");
        AuthUser user = new AuthUser(1L, "John Doe", "john.doe@example.com", "encodedPass", Role.DRIVER);
        String token = "mocked-jwt-token";

        when(authService.findUserByEmail("john.doe@example.com")).thenReturn(user);
        when(passwordValidator.validate("password123", "encodedPass")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(token);

        LoginResponseDTO response = authUseCase.login(request);

        assertEquals(token, response.accessToken());
        assertEquals(1L, response.userId());
        assertEquals("John Doe", response.name());
        assertEquals("john.doe@example.com", response.email());
        assertEquals("DRIVER", response.role());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        LoginRequestDTO request = new LoginRequestDTO("nonexistent@example.com", "password123");

        when(authService.findUserByEmail("nonexistent@example.com")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> authUseCase.login(request));
    }

    @Test
    void login_ShouldThrowException_WhenInvalidPassword() {
        LoginRequestDTO request = new LoginRequestDTO("john.doe@example.com", "wrongpass");
        AuthUser user = new AuthUser(1L, "John Doe", "john.doe@example.com", "encodedPass", Role.DRIVER);

        when(authService.findUserByEmail("john.doe@example.com")).thenReturn(user);
        when(passwordValidator.validate("wrongpass", "encodedPass")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authUseCase.login(request));
    }

    @Test
    void resetPasswordRequest_ShouldCallResetPasswordService() {
        String email = "john.doe@example.com";

        authUseCase.resetPasswordRequest(email);

        verify(resetPasswordService, times(1)).requestPasswordReset(email);
    }

    @Test
    void resetPassword_ShouldCallResetPasswordService() {
        String token = "reset-token";
        String newPassword = "newPassword123";

        authUseCase.resetPassword(token, newPassword);

        verify(resetPasswordService, times(1)).resetPassword(token, newPassword);
    }

    @Test
    void registerPassenger_ShouldReturnLoginResponse_WhenRegistrationSucceeds() {
        RegisterRequestDTO request = new RegisterRequestDTO("Jane Doe", "jane@example.com", "pass123");
        AuthUser user = new AuthUser(null, "Jane Doe", "jane@example.com", "pass123", Role.PASSENGER);
        String token = "mocked-jwt-token";

        when(authService.registerPassenger("Jane Doe", "jane@example.com", "pass123")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(token);

        LoginResponseDTO response = authUseCase.registerPassenger(request);

        assertEquals(token, response.accessToken());
        assertEquals("Jane Doe", response.name());
        assertEquals("jane@example.com", response.email());
        assertEquals("PASSENGER", response.role());
    }

    @Test
    void registerPassenger_ShouldThrowException_WhenRegistrationFails() {
        RegisterRequestDTO request = new RegisterRequestDTO("Jane Doe", "jane@example.com", "pass123");
        when(authService.registerPassenger("Jane Doe", "jane@example.com", "pass123")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> authUseCase.registerPassenger(request));
    }

    @Test
    void loginPassenger_ShouldReturnLoginResponse_WhenCredentialsAreValid() {
        LoginRequestDTO request = new LoginRequestDTO("passenger@example.com", "password123");
        AuthUser passenger = new AuthUser(2L, "Jane Passenger", "passenger@example.com", "encodedPass", Role.PASSENGER);
        String token = "mocked-passenger-jwt-token";

        when(authService.findPassengerByEmail("passenger@example.com")).thenReturn(passenger);
        when(passwordValidator.validate("password123", "encodedPass")).thenReturn(true);
        when(jwtService.generateToken(passenger)).thenReturn(token);

        LoginResponseDTO response = authUseCase.loginPassenger(request);

        assertEquals(token, response.accessToken());
        assertEquals(2L, response.userId());
        assertEquals("Jane Passenger", response.name());
        assertEquals("passenger@example.com", response.email());
        assertEquals("PASSENGER", response.role());
    }

    @Test
    void loginPassenger_ShouldThrowException_WhenPassengerNotFound() {
        LoginRequestDTO request = new LoginRequestDTO("missing@example.com", "password");

        when(authService.findPassengerByEmail("missing@example.com")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> authUseCase.loginPassenger(request));
    }


    @Test
    void loginPassenger_ShouldThrowException_WhenInvalidPassword() {
        LoginRequestDTO request = new LoginRequestDTO("passenger@example.com", "wrongpass");
        AuthUser passenger = new AuthUser(2L, "Jane Passenger", "passenger@example.com", "encodedPass", Role.PASSENGER);

        when(authService.findPassengerByEmail("passenger@example.com")).thenReturn(passenger);
        when(passwordValidator.validate("wrongpass", "encodedPass")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authUseCase.loginPassenger(request));
    }
}
