package com.gtu.auth_service.domain.service;

import com.gtu.auth_service.application.dto.LoginRequestDTO;
import com.gtu.auth_service.application.dto.LoginResponseDTO;
import com.gtu.auth_service.domain.model.AuthUser;

public interface AuthService {
    LoginResponseDTO authenticate(LoginRequestDTO request);
    AuthUser findUserByEmail(String email);
}
