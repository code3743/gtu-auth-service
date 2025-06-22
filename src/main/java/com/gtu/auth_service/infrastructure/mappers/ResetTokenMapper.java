package com.gtu.auth_service.infrastructure.mappers;

import com.gtu.auth_service.domain.model.ResetToken;
import com.gtu.auth_service.infrastructure.entities.ResetTokenEntity;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResetTokenMapper {
    
    public ResetTokenEntity toEntity(ResetToken resetToken) {
        return new ResetTokenEntity(
            resetToken.getId(),
            resetToken.getToken(),
            resetToken.getEmail(),
            resetToken.getExpiryDate(),
            resetToken.isUsed()
        );
    }

    public ResetToken toDomain(ResetTokenEntity entity) {
        return new ResetToken(
            entity.getId(),
            entity.getToken(),
            entity.getEmail(),
            entity.getExpiryDate(),
            entity.isUsed()
        );
    }
}
