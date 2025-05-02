package com.gtu.auth_service.application.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequestDTO(
        @NotNull(message = "Email cannot be null")
        String email,
        @NotNull(message = "Password cannot be null")
        String password
) {}

