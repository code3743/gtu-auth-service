package com.gtu.auth_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Passenger {
    private String name;
    private String email;
    private String password;

}
