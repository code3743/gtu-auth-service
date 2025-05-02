package com.gtu.auth_service.domain.service;

import com.gtu.auth_service.domain.model.AuthUser;

public interface JwtService {
    String generateToken(AuthUser user);
    long getExpirationTime();
}
