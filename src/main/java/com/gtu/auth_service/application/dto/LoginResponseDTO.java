package com.gtu.auth_service.application.dto;

public record LoginResponseDTO(
    String accessToken,
    Long userId,
    String name,
    String email,
    String role
) {}
