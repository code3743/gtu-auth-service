
package com.gtu.auth_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gtu.auth_service.infrastructure.client.dto.UserServiceResponse;

@FeignClient(name = "gtu-users-management-service", contextId = "passengerClient", path = "/internal/passengers")
public interface PassengerClient {
    
    @GetMapping
    UserServiceResponse getPassengerByEmail(@RequestParam String email);

}
