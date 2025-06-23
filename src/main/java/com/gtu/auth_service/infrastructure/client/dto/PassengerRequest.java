package com.gtu.auth_service.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PassengerRequest {
    private String name;
    private String email;
    private String password;
}
