package com.gtu.auth_service.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtu.auth_service.domain.exception.GeneralException;
import com.gtu.auth_service.domain.model.ResetToken;
import com.gtu.auth_service.domain.model.Role;
import com.gtu.auth_service.domain.repository.ResetTokenRepository;
import com.gtu.auth_service.infrastructure.client.PassengerClient;
import com.gtu.auth_service.infrastructure.client.UserClient;
import com.gtu.auth_service.infrastructure.client.dto.UserServiceResponse;
import com.gtu.auth_service.infrastructure.logs.LogPublisher;
import com.gtu.auth_service.infrastructure.messaging.event.ResetPasswordEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LogPublisher logPublisher;

    @InjectMocks
    private ResetPasswordServiceImpl resetPasswordService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        resetPasswordService = new ResetPasswordServiceImpl(
                userClient,
                passengerClient,
                resetTokenRepository,
                rabbitTemplate,
                objectMapper,
                logPublisher
        );
        setField("resetLinkBase", "http://reset/base"); 
    }

    private void setField(String name, String value) throws Exception {
        var field = ResetPasswordServiceImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(resetPasswordService, value);
    }

    @Test
    void requestPasswordReset_shouldSucceedForUser() throws Exception {
        UserServiceResponse user = new UserServiceResponse(1L, "user@gtu.com", null, null, null);
        when(userClient.getUserByEmail("user@gtu.com")).thenReturn(user);
        when(resetTokenRepository.findByEmailAndUsedFalse("user@gtu.com")).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any(ResetPasswordEvent.class))).thenReturn("{\"test\":\"json\"}");

        resetPasswordService.requestPasswordReset("user@gtu.com");

        verify(resetTokenRepository).save(any(ResetToken.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());
        verify(logPublisher).sendLog(anyString(), eq("auth-service"), eq("INFO"),eq("Reset email event sent successfully"),anyMap());
    }

    @Test
    void requestPasswordReset_shouldSucceedForPassenger() throws Exception {
        UserServiceResponse user = new UserServiceResponse(null, "passenger@gtu.com", null, null, null);
        UserServiceResponse passenger = new UserServiceResponse(2L, "passenger@gtu.com", null, null, null);
        when(userClient.getUserByEmail("passenger@gtu.com")).thenReturn(user);
        when(passengerClient.getPassengerByEmail("passenger@gtu.com")).thenReturn(passenger);
        when(resetTokenRepository.findByEmailAndUsedFalse("passenger@gtu.com")).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any(ResetPasswordEvent.class))).thenReturn("{\"test\":\"json\"}");

        resetPasswordService.requestPasswordReset("passenger@gtu.com");

        verify(passengerClient).getPassengerByEmail("passenger@gtu.com");
        verify(resetTokenRepository).save(any(ResetToken.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void requestPasswordReset_shouldThrowGeneralExceptionWhenTokenPending() {
        UserServiceResponse user = new UserServiceResponse(1L, "user@gtu.com", null, null, null);
        when(userClient.getUserByEmail("user@gtu.com")).thenReturn(user);
        when(resetTokenRepository.findByEmailAndUsedFalse("user@gtu.com")).thenReturn(Optional.of(new ResetToken()));

        GeneralException exception = assertThrows(GeneralException.class, () ->
            resetPasswordService.requestPasswordReset("user@gtu.com"));
        assertEquals("A reset token is already pending for this email: user@gtu.com", exception.getMessage());
        assertEquals(409, exception.getStatusCode());
    }

    @Test
    void generateResetLink_shouldReturnBaseLinkWithToken() {
        UserServiceResponse user = new UserServiceResponse(1L, "user@gtu.com", null, null, null);
        String token = "test-token";

        String resetLink = resetPasswordService.generateResetLink(user, token);

        assertEquals("http://reset/base?token=test-token", resetLink);
    }

    @Test
    void sendResetEmailEvent_shouldSucceedWhenValid() throws Exception {
        when(objectMapper.writeValueAsString(any(ResetPasswordEvent.class))).thenReturn("{\"test\":\"json\"}");

        resetPasswordService.sendResetEmailEvent("user@gtu.com", Role.ADMIN, "http://reset/base?token=test");

        verify(rabbitTemplate).convertAndSend("reset-password.exchange", "reset-password.routingkey", "{\"test\":\"json\"}");
        verify(logPublisher).sendLog(anyString(), eq("auth-service"), eq("INFO"), anyString(), anyMap());
    }

    @Test
    void resetPassword_shouldSucceedForUser() throws Exception {
        ResetToken token = new ResetToken();
        token.setToken("valid-token");
        token.setEmail("user@gtu.com");
        token.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        token.setUsed(false);
        when(resetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(userClient.getUserByEmail("user@gtu.com")).thenReturn(new UserServiceResponse(1L, "user@gtu.com", null, null, null));
        doNothing().when(userClient).resetPassword(1L, "NewPass1");

        resetPasswordService.resetPassword("valid-token", "NewPass1");

        verify(userClient).resetPassword(1L, "NewPass1");
        verify(resetTokenRepository).save(token);
    }

    @Test
    void resetPassword_shouldSucceedForPassenger() throws Exception {
        ResetToken token = new ResetToken();
        token.setToken("valid-token");
        token.setEmail("passenger@gtu.com");
        token.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        token.setUsed(false);
        when(resetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(userClient.getUserByEmail("passenger@gtu.com")).thenReturn(new UserServiceResponse(null, "passenger@gtu.com", null, null, null));
        when(passengerClient.getPassengerByEmail("passenger@gtu.com")).thenReturn(new UserServiceResponse(2L, "passenger@gtu.com", null, null, null));
        doNothing().when(passengerClient).resetPassword(2L, "NewPass1");

        resetPasswordService.resetPassword("valid-token", "NewPass1");

        verify(passengerClient).resetPassword(2L, "NewPass1");
        verify(resetTokenRepository).save(token);
    }

    @Test
    void resetPassword_shouldThrowExceptionWhenTokenInvalid() {
        when(resetTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            resetPasswordService.resetPassword("invalid-token", "NewPass1"));
        assertEquals("Invalid or expired token", exception.getMessage());
        verify(logPublisher).sendLog(anyString(), eq("auth-service"), eq("ERROR"), anyString(), anyMap());
    }

    @Test
    void resetPassword_shouldThrowExceptionWhenTokenExpired() {
        ResetToken token = new ResetToken();
        token.setToken("expired-token");
        token.setExpiryDate(LocalDateTime.now().minusMinutes(30));
        token.setUsed(false);
        when(resetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            resetPasswordService.resetPassword("expired-token", "NewPass1"));
        assertEquals("Token has expired or already used", exception.getMessage());
    }

    @Test
    void getUserIdByEmail_shouldReturnIdWhenFound() {
        UserServiceResponse user = new UserServiceResponse(1L, "user@gtu.com", null, null, null);
        when(userClient.getUserByEmail("user@gtu.com")).thenReturn(user);

        Long id = resetPasswordService.getUserIdByEmail("user@gtu.com");

        assertEquals(1L, id);
        verify(userClient).getUserByEmail("user@gtu.com");
    }

    @Test
    void getUserIdByEmail_shouldReturnNullWhenNotFound() {
        when(userClient.getUserByEmail("notfound@gtu.com")).thenThrow(new RuntimeException("Not found"));

        Long id = resetPasswordService.getUserIdByEmail("notfound@gtu.com");

        assertNull(id);
        verify(userClient).getUserByEmail("notfound@gtu.com");
    }

    @Test
    void getPassengerIdByEmail_shouldReturnIdWhenFound() {
        UserServiceResponse passenger = new UserServiceResponse(2L, "passenger@gtu.com", null, null, null);
        when(passengerClient.getPassengerByEmail("passenger@gtu.com")).thenReturn(passenger);

        Long id = resetPasswordService.getPassengerIdByEmail("passenger@gtu.com");

        assertEquals(2L, id);
        verify(passengerClient).getPassengerByEmail("passenger@gtu.com");
    }

    @Test
    void getPassengerIdByEmail_shouldReturnNullWhenNotFound() {
        when(passengerClient.getPassengerByEmail("notfound@gtu.com")).thenThrow(new RuntimeException("Not found"));

        Long id = resetPasswordService.getPassengerIdByEmail("notfound@gtu.com");

        assertNull(id);
        verify(passengerClient).getPassengerByEmail("notfound@gtu.com");
    }
}