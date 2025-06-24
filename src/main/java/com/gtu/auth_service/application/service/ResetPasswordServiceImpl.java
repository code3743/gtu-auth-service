package com.gtu.auth_service.application.service;

import com.gtu.auth_service.domain.model.ResetToken;
import com.gtu.auth_service.domain.model.Role;
import com.gtu.auth_service.domain.repository.ResetTokenRepository;
import com.gtu.auth_service.domain.service.ResetPasswordService;
import com.gtu.auth_service.infrastructure.client.PassengerClient;
import com.gtu.auth_service.infrastructure.client.UserClient;
import com.gtu.auth_service.infrastructure.client.dto.UserServiceResponse;
import com.gtu.auth_service.infrastructure.messaging.event.ResetPasswordEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ResetPasswordServiceImpl implements ResetPasswordService {

    private final UserClient userClient;
    private final PassengerClient passengerClient;
    private final ResetTokenRepository resetTokenRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${reset.links.passenger}")
    private String passengerResetLink;

    @Value("${reset.links.driver}")
    private String driverResetLink;

    @Value("${reset.links.admin}")
    private String adminResetLink;

    @Value("${reset.links.superadmin}")
    private String superadminResetLink;

    private static final String resetExchange = "reset-password.exchange";
    private static final String resetRoutingKey = "reset-password.routingkey";

    public ResetPasswordServiceImpl(UserClient userClient, PassengerClient passengerClient,
                                   ResetTokenRepository resetTokenRepository, RabbitTemplate rabbitTemplate,
                                   ObjectMapper objectMapper) {
        this.userClient = userClient;
        this.passengerClient = passengerClient;
        this.resetTokenRepository = resetTokenRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void requestPasswordReset(String email) {
        UserServiceResponse target = null;

        try {
            target = userClient.getUserByEmail(email);
        } catch (Exception e) {
            try {
                target = passengerClient.getPassengerByEmail(email);
            } catch (Exception ex) {
                throw new IllegalArgumentException("No user or passenger found with email: " + email);
            }
        }
        resetTokenRepository.findByEmailAndUsedFalse(email).ifPresent(token -> {
            throw new IllegalStateException("A reset token is already pending for this email: " + email);
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
        String baseUrl = user.getRole() != null ? switch (user.getRole().toUpperCase()) {
            case "DRIVER" -> driverResetLink;
            case "ADMIN" -> adminResetLink;
            case "SUPERADMIN" -> superadminResetLink;
            default -> throw new IllegalArgumentException("Unsupported role: " + user.getRole());
        } : passengerResetLink;
        return baseUrl + "?token=" + token;
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
            rabbitTemplate.convertAndSend(resetExchange, resetRoutingKey, message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reset email event: " + e.getMessage());
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
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