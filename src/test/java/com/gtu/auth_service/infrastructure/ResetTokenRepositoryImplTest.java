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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
        assertEquals("token123", result.get().getToken());
    }

    @Test
    void findByToken_ShouldReturnEmpty_WhenTokenNotExists() {
        when(jpaResetTokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

        Optional<ResetToken> result = resetTokenRepository.findByToken("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_ShouldReturnResetToken_WhenExists() {
        ResetTokenEntity entity = new ResetTokenEntity(2L, "token456", "email2@example.com", LocalDateTime.now(), false);
        when(jpaResetTokenRepository.findByEmailAndUsedFalse("email2@example.com")).thenReturn(Optional.of(entity));

        Optional<ResetToken> result = resetTokenRepository.findByEmailAndUsedFalse("email2@example.com");

        assertTrue(result.isPresent());
        assertEquals("token456", result.get().getToken());
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailNotExists() {
        when(jpaResetTokenRepository.findByEmailAndUsedFalse("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<ResetToken> result = resetTokenRepository.findByEmailAndUsedFalse("nonexistent@example.com");

        assertFalse(result.isPresent());
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

    @Test
    void save_ShouldInvoke_JpaRepositorySave() {
        ResetToken domainToken = new ResetToken();
        domainToken.setId(3L);
        domainToken.setToken("token789");
        domainToken.setEmail("save@example.com");
        domainToken.setExpiryDate(LocalDateTime.now());
        domainToken.setUsed(false);

        ResetTokenEntity entity = new ResetTokenEntity(3L, "token789", "save@example.com", domainToken.getExpiryDate(), false);
        when(jpaResetTokenRepository.save(any(ResetTokenEntity.class))).thenReturn(entity);

        resetTokenRepository.save(domainToken);

        verify(jpaResetTokenRepository).save(any(ResetTokenEntity.class));
    }
}