package com.gtu.auth_service.domain.service;

import com.gtu.auth_service.domain.model.Role;
import com.gtu.auth_service.infrastructure.client.dto.UserServiceResponse;

public interface ResetPasswordService {
    void requestPasswordReset(String email);
    String generateResetLink(UserServiceResponse user, String token);
    void sendResetEmailEvent(String to, Role role, String resetLink);
    void resetPassword(String token, String newPassword);
}
