package com.gtu.auth_service.infrastructure.messaging.event;

import com.gtu.auth_service.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordEvent {
    private String to;
    private Role role;
    private String resetLink;
}