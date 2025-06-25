package com.gtu.auth_service.presentation.rest;

import com.gtu.auth_service.application.dto.LoginRequestDTO;
import com.gtu.auth_service.application.dto.LoginResponseDTO;
import com.gtu.auth_service.application.dto.RegisterRequestDTO;
import com.gtu.auth_service.application.dto.ResetPasswordDTO;
import com.gtu.auth_service.application.dto.ResetPasswordRequestDTO;
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

    @PostMapping("/login-passenger")
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> loginPassenger(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authUseCase.loginPassenger(request);
        return ResponseEntity.status(200).body(new ResponseDTO<>("Passenger login successful", response, 200));
    }

    @PostMapping("/reset-password-request")
    public ResponseEntity<ResponseDTO<Void>> resetPasswordRequest(@RequestBody ResetPasswordRequestDTO request) {
        authUseCase.resetPasswordRequest(request.getEmail());
        return ResponseEntity.ok(new ResponseDTO<>("Password reset request successful", null, 200));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseDTO<Void>> resetPassword(@RequestBody ResetPasswordDTO request) {
        authUseCase.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new ResponseDTO<>("Password reset successful", null, 200));
    }


    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> registerPassenger(@Valid @RequestBody RegisterRequestDTO request) {
        LoginResponseDTO response = authUseCase.registerPassenger(request);
        return ResponseEntity.status(201).body(new ResponseDTO<>("Registration successful", response, 201));
    }
}