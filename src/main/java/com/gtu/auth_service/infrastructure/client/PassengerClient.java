
package com.gtu.auth_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gtu.auth_service.infrastructure.client.dto.PassengerRequest;
import com.gtu.auth_service.infrastructure.client.dto.UserServiceResponse;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@FeignClient(name = "gtu-users-management-service", contextId = "passengerClient", path = "/internal/passengers")
public interface PassengerClient {
    
    @GetMapping
    UserServiceResponse getPassengerByEmail(@RequestParam String email);

    @PutMapping("/{id}/reset-password")
    void resetPassword(@PathVariable Long id, @RequestParam String newPassword);

    @PostMapping
    UserServiceResponse registerPassenger(@RequestBody PassengerRequest request);
}
