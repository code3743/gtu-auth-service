package com.gtu.auth_service.application.usecase;

import org.springframework.stereotype.Service;

import com.gtu.auth_service.application.dto.LoginRequestDTO;
import com.gtu.auth_service.application.dto.LoginResponseDTO;
import com.gtu.auth_service.application.dto.RegisterRequestDTO;
import com.gtu.auth_service.infrastructure.security.PasswordValidator;
import com.gtu.auth_service.domain.model.AuthUser;
import com.gtu.auth_service.domain.service.AuthService;
import com.gtu.auth_service.domain.service.JwtService;
import com.gtu.auth_service.domain.service.ResetPasswordService;

@Service
public class AuthUseCase {
    private final AuthService authService;
    private final JwtService jwtService;
    private final ResetPasswordService resetPasswordService;
    private final PasswordValidator passwordValidator;

    public AuthUseCase(AuthService authService, JwtService jwtService, ResetPasswordService resetPasswordService, PasswordValidator passwordValidator) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.resetPasswordService = resetPasswordService;
        this.passwordValidator = passwordValidator;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        AuthUser user = authService.findUserByEmail(request.email());
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (!passwordValidator.validate(request.password(), user.password())) {
            throw new IllegalArgumentException("Invalid password");
        }
        String token = jwtService.generateToken(user);
        return new LoginResponseDTO(
                token,        
                user.id(),
                user.name(),
                user.email(),
                user.role().name()
        );
    }

    public LoginResponseDTO loginPassenger(LoginRequestDTO request) {
        AuthUser passenger = authService.findPassengerByEmail(request.email());
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger not found");
        }
        if (!passwordValidator.validate(request.password(), passenger.password())) {
            throw new IllegalArgumentException("Invalid password");
        }
        String token = jwtService.generateToken(passenger);
        return new LoginResponseDTO(
                token,
                passenger.id(),
                passenger.name(),
                passenger.email(),
                passenger.role().name()
        );
    }

    public void resetPasswordRequest(String email){
        resetPasswordService.requestPasswordReset(email);
    }

    public void resetPassword(String token, String newPassword){
        resetPasswordService.resetPassword(token, newPassword);
    }


    public LoginResponseDTO registerPassenger(RegisterRequestDTO request) {
        AuthUser user = authService.registerPassenger(request.getName(), request.getEmail(), request.getPassword());
        if (user == null) {
            throw new IllegalArgumentException("Registration failed");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponseDTO(
                token,
                user.id(),
                user.name(),
                user.email(),
                user.role().name()
        );
    }
}
