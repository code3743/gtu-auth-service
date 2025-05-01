package com.gtu.auth_service.presentation.rest;

import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtu.auth_service.infraestructure.client.PassengerClient;
import com.gtu.auth_service.infraestructure.client.UserClient;
import com.gtu.auth_service.infraestructure.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/test")
@Tag(name = "Auth", description = "Endpoints for managing authentication")
@CrossOrigin(origins = "*")
public class AuthController {
    private UserClient userClient;
    private PassengerClient passengerClient;
    private JwtService jwtService;

    

    public AuthController(UserClient userClient, PassengerClient passengerClient, JwtService jwtService) {
        this.userClient = userClient;
        this.passengerClient = passengerClient;
        this.jwtService = jwtService;
    }
    @GetMapping
    @Operation(summary = "Test endpoint", description = "A test endpoint to check the service")
    public ResponseEntity<HashMap<String, String>> test() {
        final var user = userClient.getUserByEmail("admin@example.com");
        final var passenger = passengerClient.getPassengerByEmail("test@example.com");
        final var tokenUser = jwtService.generateToken(user);
        final var tokenPassager = jwtService.generateToken(passenger);
        final var response = new HashMap<String, String>();
        response.put("user", user.getName());
        response.put("passenger", passenger.getName());
        response.put("tokenUser", tokenUser);
        response.put("tokenPassenger", tokenPassager);
        return ResponseEntity.ok(response);
    }

}

