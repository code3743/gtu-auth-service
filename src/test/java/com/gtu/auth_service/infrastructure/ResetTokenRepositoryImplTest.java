package com.gtu.auth_service.infrastructure;

import com.gtu.auth_service.domain.model.ResetToken;
import com.gtu.auth_service.infrastructure.entities.ResetTokenEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class ResetTokenRepositoryImplTest {

    @Mock
    private JpaResetTokenRepository jpaResetTokenRepository;

    @InjectMocks
    private ResetTokenRepositoryImpl resetTokenRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByToken_ShouldReturnResetToken_WhenTokenExists() {
        ResetTokenEntity entity = new ResetTokenEntity(1L, "token123", "email@example.com", LocalDateTime.now(), false);
        when(jpaResetTokenRepository.findByToken("token123")).thenReturn(Optional.of(entity));

        Optional<ResetToken> result = resetTokenRepository.findByToken("token123");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        ResetTokenEntity entity = new ResetTokenEntity(1L, "token123", "email@example.com", LocalDateTime.now(), false);
        when(jpaResetTokenRepository.findByEmail("email@example.com")).thenReturn(Optional.of(entity));

        boolean result = resetTokenRepository.existsByEmail("email@example.com");

        assertTrue(result);
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailNotExists() {
        when(jpaResetTokenRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        boolean result = resetTokenRepository.existsByEmail("nonexistent@example.com");

        assertFalse(result);
    }
}