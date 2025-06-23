package com.gtu.auth_service.application.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.gtu.auth_service.application.dto.LoginRequestDTO;
import com.gtu.auth_service.application.dto.LoginResponseDTO;
import com.gtu.auth_service.domain.model.AuthUser;
import com.gtu.auth_service.domain.model.Role;
import com.gtu.auth_service.domain.service.AuthService;
import com.gtu.auth_service.infrastructure.client.UserClient;
import com.gtu.auth_service.infrastructure.client.dto.UserServiceResponse;
import com.gtu.auth_service.infrastructure.logs.LogPublisher; 

@Service
public class AuthServiceImpl implements AuthService {
    

    private final UserClient userClient;
    private final LogPublisher logPublisher;

    public AuthServiceImpl(UserClient userClient, LogPublisher logPublisher) {
        this.userClient = userClient;
        this.logPublisher = logPublisher;
    }


    @Override
    public LoginResponseDTO authenticate(LoginRequestDTO request) {
        throw new UnsupportedOperationException("Use AuthUseCase for authentication");
    }

    @Override
    public AuthUser findUserByEmail(String email) {
        try {
            UserServiceResponse user = userClient.getUserByEmail(email);
            if (user != null) {
                Role role = mapToRole(user.getRole());
                logPublisher.sendLog(
                Instant.now().toString(),
                "auth-service",
                "INFO",
                "Login Successful",
                Map.of("email", email, "userId", user.getId())
                );
                return new AuthUser(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPassword(),
                        role
                );}
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public Role mapToRole(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        return switch (role.toUpperCase()) {
            case "SUPERADMIN" -> Role.SUPERADMIN;
            case "ADMIN" -> Role.ADMIN;
            case "DRIVER" -> Role.DRIVER;
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };
    }
}