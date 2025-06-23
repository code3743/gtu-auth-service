package com.gtu.auth_service.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtu.auth_service.domain.model.ResetToken;
import com.gtu.auth_service.domain.model.Role;
import com.gtu.auth_service.domain.repository.ResetTokenRepository;
import com.gtu.auth_service.infrastructure.client.PassengerClient;
import com.gtu.auth_service.infrastructure.client.UserClient;
import com.gtu.auth_service.infrastructure.client.dto.UserServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ResetPasswordServiceImplTest {

    @Mock
    private UserClient userClient;

    @Mock
    private PassengerClient passengerClient;

    @Mock
    private ResetTokenRepository resetTokenRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ResetPasswordServiceImpl resetPasswordService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        resetPasswordService = new ResetPasswordServiceImpl(
                userClient,
                passengerClient,
                resetTokenRepository,
                rabbitTemplate,
                objectMapper
        );

        setField("passengerResetLink", "http://reset/passenger");
        setField("driverResetLink", "http://reset/driver");
        setField("adminResetLink", "http://reset/admin");
        setField("superadminResetLink", "http://reset/superadmin");
    }

    private void setField(String name, String value) throws Exception {
        var field = ResetPasswordServiceImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(resetPasswordService, value);
    }

    @Test
    void generateResetLink_ShouldReturnDriverResetLink() {
        UserServiceResponse user = new UserServiceResponse(1L, "John", "john@example.com", "pass", "DRIVER");
        String token = UUID.randomUUID().toString();

        String result = resetPasswordService.generateResetLink(user, token);

        assertTrue(result.startsWith("http://reset/driver"));
        assertTrue(result.contains(token));
    }

    @Test
    void generateResetLink_ShouldReturnPassengerLink_WhenRoleIsNull() {
        UserServiceResponse user = new UserServiceResponse(1L, "Jane", "jane@example.com", "pass", null);
        String token = "abc123";

        String result = resetPasswordService.generateResetLink(user, token);

        assertEquals("http://reset/passenger?token=abc123", result);
    }

    @Test
    void generateResetLink_ShouldThrowException_ForInvalidRole() {
        UserServiceResponse user = new UserServiceResponse(1L, "Fake", "fake@example.com", "pass", "HACKER");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                resetPasswordService.generateResetLink(user, "token123"));

        assertEquals("Unsupported role: HACKER", ex.getMessage());
    }

    @Test
    void getUserIdByEmail_ShouldReturnId_WhenUserExists() {
        UserServiceResponse user = new UserServiceResponse(10L, "X", "x@example.com", "pass", "ADMIN");
        when(userClient.getUserByEmail("x@example.com")).thenReturn(user);

        Long result = resetPasswordService.getUserIdByEmail("x@example.com");

        assertEquals(10L, result);
    }

    @Test
    void getUserIdByEmail_ShouldReturnNull_WhenNotFound() {
        when(userClient.getUserByEmail("x@example.com")).thenThrow(RuntimeException.class);

        Long result = resetPasswordService.getUserIdByEmail("x@example.com");

        assertNull(result);
    }

    @Test
    void sendResetEmailEvent_ShouldPublishMessage() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());

        assertDoesNotThrow(() ->
                resetPasswordService.sendResetEmailEvent("user@example.com", Role.ADMIN, "http://reset/admin?token=abc"));
    }

    @Test
    void sendResetEmailEvent_ShouldThrowException_WhenObjectMapperFails() throws Exception {
        ResetPasswordServiceImpl service = new ResetPasswordServiceImpl(
                userClient, passengerClient, resetTokenRepository, rabbitTemplate, mock(ObjectMapper.class)
        );
        setFieldOnInstance(service, "adminResetLink", "http://reset/admin");
        setFieldOnInstance(service, "driverResetLink", "http://reset/driver");
        setFieldOnInstance(service, "passengerResetLink", "http://reset/passenger");
        setFieldOnInstance(service, "superadminResetLink", "http://reset/superadmin");

        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Mapper error"));

        var serviceWithFailingMapper = new ResetPasswordServiceImpl(
                userClient, passengerClient, resetTokenRepository, rabbitTemplate, failingMapper);

        setFieldOnInstance(serviceWithFailingMapper, "adminResetLink", "http://reset/admin");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                serviceWithFailingMapper.sendResetEmailEvent("x", Role.ADMIN, "http://link"));

        assertTrue(ex.getMessage().contains("Failed to send reset email event"));
    }

    private void setFieldOnInstance(Object target, String fieldName, String value) throws Exception {
        var field = ResetPasswordServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void requestPasswordReset_ShouldSaveToken_WhenUserExists() {
        UserServiceResponse user = new UserServiceResponse(1L, "John", "john@example.com", "pass", "DRIVER");
        when(userClient.getUserByEmail("john@example.com")).thenReturn(user);
        when(resetTokenRepository.findByEmailAndUsedFalse("john@example.com")).thenReturn(Optional.empty());

        resetPasswordService.requestPasswordReset("john@example.com");

        verify(resetTokenRepository, times(1)).save(any());
    }

    @Test
    void requestPasswordReset_ShouldThrowException_WhenTokenExists() {
        UserServiceResponse user = new UserServiceResponse(1L, "John", "john@example.com", "pass", "DRIVER");
        when(userClient.getUserByEmail("john@example.com")).thenReturn(user);
        when(resetTokenRepository.findByEmailAndUsedFalse("john@example.com")).thenReturn(Optional.of(new ResetToken()));

        assertThrows(IllegalStateException.class, () -> resetPasswordService.requestPasswordReset("john@example.com"));
    }

    @Test
    void resetPassword_ShouldCallUserClient_WhenUserExists() {
        ResetToken resetToken = new ResetToken(1L, "token123", "john@example.com", LocalDateTime.now().plusMinutes(10), false);
        when(resetTokenRepository.findByToken("token123")).thenReturn(Optional.of(resetToken));
        when(userClient.getUserByEmail("john@example.com")).thenReturn(new UserServiceResponse(1L, "John", "john@example.com", "pass", "DRIVER"));

        resetPasswordService.resetPassword("token123", "newPass");

        verify(userClient, times(1)).resetPassword(1L, "newPass");
        verify(resetTokenRepository, times(1)).save(any());
    }

    @Test
    void resetPassword_ShouldThrowException_WhenNoUserOrPassengerFound() {
        ResetToken resetToken = new ResetToken(1L, "token123", "nonexistent@example.com", LocalDateTime.now().plusMinutes(10), false);
        when(resetTokenRepository.findByToken("token123")).thenReturn(Optional.of(resetToken));
        when(userClient.getUserByEmail("nonexistent@example.com")).thenThrow(RuntimeException.class);
        when(passengerClient.getPassengerByEmail("nonexistent@example.com")).thenThrow(RuntimeException.class);

        assertThrows(IllegalArgumentException.class, () ->
                resetPasswordService.resetPassword("token123", "newPass"));
    }
}
