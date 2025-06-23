package com.gtu.auth_service.infrastructure.mappers;

import com.gtu.auth_service.domain.model.ResetToken;
import com.gtu.auth_service.infrastructure.entities.ResetTokenEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResetTokenMapperTest {

    @Test
    void toEntity_ShouldMapResetTokenToEntity() {
        ResetToken resetToken = new ResetToken(1L, "token123", "email@example.com", LocalDateTime.now(), false);
        ResetTokenEntity entity = ResetTokenMapper.toEntity(resetToken);

        assertEquals(1L, entity.getId());
        assertEquals("token123", entity.getToken());
        assertEquals("email@example.com", entity.getEmail());
        assertEquals(resetToken.getExpiryDate(), entity.getExpiryDate());
        assertEquals(false, entity.isUsed());
    }

    @Test
    void toDomain_ShouldMapEntityToResetToken() {
        ResetTokenEntity entity = new ResetTokenEntity(1L, "token123", "email@example.com", LocalDateTime.now(), false);
        ResetToken resetToken = ResetTokenMapper.toDomain(entity);

        assertEquals(1L, resetToken.getId());
        assertEquals("token123", resetToken.getToken());
        assertEquals("email@example.com", resetToken.getEmail());
        assertEquals(entity.getExpiryDate(), resetToken.getExpiryDate());
        assertEquals(false, resetToken.isUsed());
    }
}