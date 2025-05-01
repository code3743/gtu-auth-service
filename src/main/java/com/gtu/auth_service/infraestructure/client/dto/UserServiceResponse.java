package com.gtu.auth_service.infraestructure.client.dto;

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
}
