package com.gtu.auth_service.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserServiceResponse {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String role;
}
