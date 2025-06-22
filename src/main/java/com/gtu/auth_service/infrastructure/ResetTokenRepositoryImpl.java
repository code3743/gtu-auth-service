package com.gtu.auth_service.infrastructure;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.gtu.auth_service.domain.model.ResetToken;
import com.gtu.auth_service.domain.repository.ResetTokenRepository;
import com.gtu.auth_service.infrastructure.mappers.ResetTokenMapper;

@Repository
public class ResetTokenRepositoryImpl implements ResetTokenRepository {

    private final JpaResetTokenRepository jpaResetTokenRepository;

    public ResetTokenRepositoryImpl(JpaResetTokenRepository jpaResetTokenRepository) {
        this.jpaResetTokenRepository = jpaResetTokenRepository;
    }

    @Override
    public Optional<ResetToken> findByToken(String token) {
        return jpaResetTokenRepository.findByToken(token)
                .map(ResetTokenMapper::toDomain);
    }

    @Override
    public Optional<ResetToken> findByEmailAndUsedFalse(String email) {
        return jpaResetTokenRepository.findByEmailAndUsedFalse(email)
                .map(ResetTokenMapper::toDomain);
    }

    @Override
    public void save(ResetToken resetToken) {
        jpaResetTokenRepository.save(ResetTokenMapper.toEntity(resetToken));
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaResetTokenRepository.findByEmail(email).isPresent();
    }
}
