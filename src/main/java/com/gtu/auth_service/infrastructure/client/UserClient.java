package com.gtu.auth_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gtu.auth_service.infrastructure.client.dto.UserServiceResponse;

@FeignClient(name = "gtu-users-management-service", contextId = "userClient", path = "/internal/users")
public interface UserClient {
    
    @GetMapping
    UserServiceResponse getUserByEmail(@RequestParam String email);

    @PutMapping("/{id}/reset-password")
    void resetPassword(@PathVariable Long id, @RequestParam String newPassword);
}
