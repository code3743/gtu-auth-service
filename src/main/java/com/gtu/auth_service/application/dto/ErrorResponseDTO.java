package com.gtu.auth_service.application.dto;

public record ErrorResponseDTO(
    String message,
    int status,
    String error
){}
