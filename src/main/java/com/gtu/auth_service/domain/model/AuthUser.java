package com.gtu.auth_service.domain.model;

public record AuthUser(
    Long id,
    String name,
    String email,
    String password,
    Role role
) {}

