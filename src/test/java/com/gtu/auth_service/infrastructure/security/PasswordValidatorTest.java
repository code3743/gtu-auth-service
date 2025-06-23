package com.gtu.auth_service.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorTest {
    @InjectMocks
    private PasswordValidator passwordValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validate_ShouldReturnTrue_WhenPasswordMatches() {
        String rawPassword = "password123";
        String encodedPassword = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(rawPassword);

        assertTrue(passwordValidator.validate(rawPassword, encodedPassword));
    }

    @Test
    void validate_ShouldReturnFalse_WhenPasswordDoesNotMatch() {
        String rawPassword = "password123";
        String wrongEncodedPassword = new BCryptPasswordEncoder().encode("wrongpass");

        assertFalse(passwordValidator.validate(rawPassword, wrongEncodedPassword));
    }

    @Test
    void validate_ShouldReturnFalse_WhenExceptionOccurs() {
        String rawPassword = null;
        String encodedPassword = "invalidEncoded";

        assertFalse(passwordValidator.validate(rawPassword, encodedPassword));
    }
}
