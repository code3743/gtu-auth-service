package com.gtu.auth_service.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetToken {
    private Long id;

    private String token;

    private String email;

    private LocalDateTime expiryDate;

    private boolean used;
}
