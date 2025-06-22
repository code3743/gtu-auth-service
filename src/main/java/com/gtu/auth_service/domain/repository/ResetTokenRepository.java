package com.gtu.auth_service.domain.repository;

import com.gtu.auth_service.domain.model.ResetToken;

import java.util.Optional;

public interface ResetTokenRepository {
    Optional<ResetToken> findByToken(String token);
    Optional<ResetToken> findByEmailAndUsedFalse(String email);
    void save(ResetToken resetToken);
    boolean existsByEmail(String email);
}
