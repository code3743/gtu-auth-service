package com.gtu.auth_service.application.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtu.auth_service.domain.model.ResetToken;
import com.gtu.auth_service.domain.model.Role;
import com.gtu.auth_service.domain.repository.ResetTokenRepository;
import com.gtu.auth_service.domain.service.ResetPasswordService;
import com.gtu.auth_service.infrastructure.client.PassengerClient;
import com.gtu.auth_service.infrastructure.client.UserClient;
import com.gtu.auth_service.infrastructure.client.dto.UserServiceResponse;
import com.gtu.auth_service.infrastructure.logs.LogPublisher;
import com.gtu.auth_service.infrastructure.messaging.event.ResetPasswordEvent;
import com.gtu.auth_service.domain.exception.GeneralException;

@Service
public class ResetPasswordServiceImpl implements ResetPasswordService {

    private final UserClient userClient;
    private final PassengerClient passengerClient;
    private final ResetTokenRepository resetTokenRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${reset.links.base}")
    private String resetLinkBase;

    private static final String RESET_EXCHANGE = "reset-password.exchange";
    private static final String RESET_ROUTING_KEY = "reset-password.routingkey";
    private static final String SERVICE_NAME = "auth-service";
    private static final String LOG_LEVEL_ERROR = "ERROR";
    private static final String LOG_KEY_EMAIL = "email";
    private static final String LOG_KEY_ERROR = "error";

    private final LogPublisher logPublisher;

    public ResetPasswordServiceImpl(UserClient userClient, PassengerClient passengerClient,
                                   ResetTokenRepository resetTokenRepository, RabbitTemplate rabbitTemplate,
                                   ObjectMapper objectMapper, LogPublisher logPublisher) {
        this.userClient = userClient;
        this.passengerClient = passengerClient;
        this.resetTokenRepository = resetTokenRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.logPublisher = logPublisher;
    }

    @Override
    public void requestPasswordReset(String email) {
        UserServiceResponse target = null;

        try {
            target = userClient.getUserByEmail(email);

            if (target.getId() == null) {
                target = passengerClient.getPassengerByEmail(email);
            }
        } catch (Exception e) {
            logPublisher.sendLog(
                Instant.now().toString(),
                SERVICE_NAME,
                LOG_LEVEL_ERROR,
                "Error fetching user or passenger from clients",
                Map.of(LOG_KEY_EMAIL, email, LOG_KEY_ERROR, e.getMessage())
            );
            throw new GeneralException(e.getMessage(), 500);
        }

        if (target.getId() == null) {
            throw new GeneralException("No user or passenger found with email: " + email, 404);
        }

        resetTokenRepository.findByEmailAndUsedFalse(email).ifPresent(token -> {
            throw new GeneralException("A reset token is already pending for this email: " + email, 409);
        });

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        ResetToken resetToken = new ResetToken();
        resetToken.setToken(token);
        resetToken.setEmail(email);
        resetToken.setExpiryDate(expiryDate);
        resetTokenRepository.save(resetToken);

        String resetLink = generateResetLink(target, token);
        sendResetEmailEvent(email, getRole(target), resetLink);
    }

    @Override
    public String generateResetLink(UserServiceResponse user, String token) {
        return resetLinkBase + "?token=" + token; 
    }

    private Role getRole(UserServiceResponse user) {
        return user.getRole() != null ? mapToRole(user.getRole()) : Role.PASSENGER;
    }

    private Role mapToRole(String role) {
        if (role == null) return Role.PASSENGER;
        return switch (role.toUpperCase()) {
            case "SUPERADMIN" -> Role.SUPERADMIN;
            case "ADMIN" -> Role.ADMIN;
            case "DRIVER" -> Role.DRIVER;
            default -> Role.PASSENGER;
        };
    }

    @Override
    public void sendResetEmailEvent(String to, Role role, String resetLink) {
        ResetPasswordEvent event = new ResetPasswordEvent(to, role, resetLink);
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(RESET_EXCHANGE, RESET_ROUTING_KEY, message);

            logPublisher.sendLog(
            Instant.now().toString(),
            SERVICE_NAME,
            "INFO",
            "Reset email event sent successfully",
            Map.of(LOG_KEY_EMAIL, to, "role", role.name(), "resetLink", resetLink)
        );
        } catch (Exception e) {
            logPublisher.sendLog(
            Instant.now().toString(),
            SERVICE_NAME,
            LOG_LEVEL_ERROR,
            "Failed to send reset email event",
            Map.of(LOG_KEY_EMAIL, to, "role", role.name(), "resetLink", resetLink, LOG_KEY_ERROR, e.getMessage())
            );
            throw new RuntimeException("Failed to send reset email event: " + e.getMessage());
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        try{
            ResetToken resetToken = resetTokenRepository.findByToken(token)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

            if (resetToken.getExpiryDate().isBefore(LocalDateTime.now()) || resetToken.isUsed()) {
                throw new IllegalArgumentException("Token has expired or already used");
            }

            Long userId = getUserIdByEmail(resetToken.getEmail());
            if (userId != null) {
                userClient.resetPassword(userId, newPassword);
            } else {
                Long passengerId = getPassengerIdByEmail(resetToken.getEmail());
                if (passengerId != null) {
                    passengerClient.resetPassword(passengerId, newPassword);
                } else {
                    throw new IllegalArgumentException("No user or passenger found with email: " + resetToken.getEmail());
                }
            }

            resetToken.setUsed(true);
            resetTokenRepository.save(resetToken);

        } catch (Exception e) {
            logPublisher.sendLog(
                Instant.now().toString(),
                SERVICE_NAME,
                LOG_LEVEL_ERROR,
                "Failed to reset password",
                Map.of("token", token, LOG_KEY_ERROR, e.getMessage())
            );
            throw e;
        }
    }

    public Long getUserIdByEmail(String email) {
        try {
            UserServiceResponse user = userClient.getUserByEmail(email);
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Long getPassengerIdByEmail(String email) {
        try {
            UserServiceResponse passenger = passengerClient.getPassengerByEmail(email);
            return passenger != null ? passenger.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}