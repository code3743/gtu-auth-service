package com.gtu.auth_service.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordValidator() {
        this.passwordEncoder = new BCryptPasswordEncoder(10);
    }

    public boolean validate(String rawPassword, String encodedPassword) {
        try {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}