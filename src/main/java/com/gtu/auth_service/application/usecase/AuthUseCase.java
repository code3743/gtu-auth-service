package com.gtu.auth_service.application.usecase;

import org.springframework.stereotype.Service;

import com.gtu.auth_service.application.dto.LoginRequestDTO;
import com.gtu.auth_service.application.dto.LoginResponseDTO;
import com.gtu.auth_service.infrastructure.security.PasswordValidator;
import com.gtu.auth_service.domain.model.AuthUser;
import com.gtu.auth_service.domain.service.AuthService;
import com.gtu.auth_service.domain.service.JwtService;

@Service
public class AuthUseCase {
    private final AuthService authService;
    private final JwtService jwtService;
    private final PasswordValidator PasswordValidator;

    public AuthUseCase(AuthService authService, JwtService jwtService, PasswordValidator PasswordValidator) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.PasswordValidator = PasswordValidator;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        AuthUser user = authService.findUserByEmail(request.email());
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (!PasswordValidator.validate(request.password(), user.password())) {
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
}
