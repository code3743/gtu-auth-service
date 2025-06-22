package com.gtu.auth_service.infrastructure;

import org.springframework.stereotype.Repository;

import com.gtu.auth_service.infrastructure.entities.ResetTokenEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
@Repository
public interface JpaResetTokenRepository extends JpaRepository<ResetTokenEntity, Long> {
    @Query("SELECT rt FROM ResetTokenEntity rt WHERE rt.token = ?1")
    Optional<ResetTokenEntity> findByToken(String token);

    @Query("SELECT rt FROM ResetTokenEntity rt WHERE rt.email = ?1 AND rt.used = false")
    Optional<ResetTokenEntity> findByEmailAndUsedFalse(String email);

    @Query("SELECT rt FROM ResetTokenEntity rt WHERE rt.email = ?1")
    Optional<ResetTokenEntity> findByEmail(String email);
}
