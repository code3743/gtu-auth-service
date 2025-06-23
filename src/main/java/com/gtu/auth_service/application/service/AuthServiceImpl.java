package com.gtu.auth_service.application.service;

import com.gtu.auth_service.application.dto.LoginRequestDTO;
import com.gtu.auth_service.application.dto.LoginResponseDTO;
import com.gtu.auth_service.application.dto.RegisterRequestDTO;
import com.gtu.auth_service.domain.model.AuthUser;
import com.gtu.auth_service.domain.model.Role;
import com.gtu.auth_service.domain.service.AuthService;
import com.gtu.auth_service.infrastructure.client.PassengerClient;
import com.gtu.auth_service.infrastructure.client.UserClient;
import com.gtu.auth_service.infrastructure.client.dto.UserServiceResponse;

import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserClient userClient;
    private final PassengerClient passengerClient;

    public AuthServiceImpl(UserClient userClient, PassengerClient passengerClient) {
        this.userClient = userClient;
        this.passengerClient = passengerClient;
    }

    @Override
    public LoginResponseDTO authenticate(LoginRequestDTO request) {
        throw new UnsupportedOperationException("Use AuthUseCase for authentication");
    }

    @Override
    public AuthUser findUserByEmail(String email) {
        UserServiceResponse user = userClient.getUserByEmail(email);
        if (user != null) {
            Role role = mapToRole(user.getRole());
            return new AuthUser(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    role
            );
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

    @Override
    public AuthUser registerPassenger(String name, String email, String password) {
       var newPassenger = passengerClient.registerPassenger(
                new RegisterRequestDTO(name, email, password));
        if (newPassenger == null) throw new  IllegalArgumentException("Failed to register passenger");

        return new AuthUser(
                null,
                newPassenger.getName(),
                newPassenger.getEmail(),
                newPassenger.getPassword(),
                Role.PASSENGER 
        );
    }

    @Override
    public AuthUser findPassengerByEmail(String email) {
        UserServiceResponse passengerResponse = passengerClient.getPassengerByEmail(email);
        if (passengerResponse != null) {
            return new AuthUser(
                    passengerResponse.getId(),
                    passengerResponse.getName(),
                    passengerResponse.getEmail(),
                    passengerResponse.getPassword(),
                    Role.PASSENGER
            );
        }
        return null;
    }

}