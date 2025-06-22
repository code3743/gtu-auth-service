package com.gtu.auth_service.presentation.rest;

import com.gtu.auth_service.application.dto.LoginRequestDTO;
import com.gtu.auth_service.application.dto.LoginResponseDTO;
import com.gtu.auth_service.application.dto.RegisterRequestDTO;
import com.gtu.auth_service.application.usecase.AuthUseCase;
import com.gtu.auth_service.application.dto.ResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authUseCase.login(request);
        return ResponseEntity.status(200).body(new ResponseDTO<>("Login successful", response, 200));
    }

    @PostMapping("/reset-password-request")
    public ResponseEntity<ResponseDTO<Void>> resetPasswordRequest(@RequestParam String email) {
        authUseCase.resetPasswordRequest(email);
        return ResponseEntity.ok(new ResponseDTO<>("Password reset request successful", null, 200));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseDTO<Void>> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authUseCase.resetPassword(token, newPassword);
        return ResponseEntity.ok(new ResponseDTO<>("Password reset successful", null, 200));
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> registerPassenger(@Valid @RequestBody RegisterRequestDTO request) {
        LoginResponseDTO response = authUseCase.registerPassenger(request);
        return ResponseEntity.status(201).body(new ResponseDTO<>("Registration successful", response, 201));
    }
}