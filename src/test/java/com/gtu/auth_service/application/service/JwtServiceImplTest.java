package com.gtu.auth_service.application.service;

import com.gtu.auth_service.domain.model.AuthUser;
import com.gtu.auth_service.domain.model.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.lang.reflect.Field;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtServiceImplTest {
    @InjectMocks
    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtServiceImpl();

        String rawKey = "testSecretKeyForJWTtestSecretKeyForJWT"; 
        String base64Key = Base64.getEncoder().encodeToString(rawKey.getBytes());

        Field field = JwtServiceImpl.class.getDeclaredField("secretKey");
        field.setAccessible(true);
        field.set(jwtService, base64Key);
    }

    @Test
    void generateToken_ShouldReturnValidJwtToken_WhenUserProvided() {
        AuthUser user = new AuthUser(1L, "John Doe", "john.doe@example.com", "password", Role.DRIVER);
        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getExpirationTime_ShouldReturnDefault30Minutes() {
        long expiration = jwtService.getExpirationTime();
        assertEquals(30L * 60000, expiration); 
    }

    @Test
    void generateToken_WhenFieldsAreNull_ShouldStillGenerateToken() {
        AuthUser user = new AuthUser(null, null, null, null, Role.DRIVER);
        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
}