package com.gtu.auth_service.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtu.auth_service.application.dto.LoginRequestDTO;
import com.gtu.auth_service.application.dto.LoginResponseDTO;
import com.gtu.auth_service.application.dto.RegisterRequestDTO;
import com.gtu.auth_service.application.usecase.AuthUseCase;
import com.gtu.auth_service.presentation.exception.GlobalExceptionHandler;
import com.gtu.auth_service.infrastructure.logs.LogPublisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

        private MockMvc mockMvc;

        @Mock
        private AuthUseCase authUseCase;

        @Mock
        private LogPublisher logPublisher;

        @InjectMocks
        private AuthController authController;

        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();

                mockMvc = MockMvcBuilders.standaloneSetup(authController)
                                .setControllerAdvice(new GlobalExceptionHandler((Mockito.mock(LogPublisher.class))))
                                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                                .build();
        }

        @Test
        void login_ShouldReturnLoginResponseDTO_WhenCredentialsAreValid() throws Exception {
                LoginRequestDTO request = new LoginRequestDTO("john@example.com", "password123");
                LoginResponseDTO response = new LoginResponseDTO("jwt-token", 1L, "John", "john@example.com", "DRIVER");

                Mockito.when(authUseCase.login(Mockito.any(LoginRequestDTO.class))).thenReturn(response);

                mockMvc.perform(post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Login successful"))
                                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                                .andExpect(jsonPath("$.data.email").value("john@example.com"))
                                .andExpect(jsonPath("$.data.role").value("DRIVER"));
        }

        @Test
        void login_ShouldReturnBadRequest_WhenUserNotFound() throws Exception {
                LoginRequestDTO request = new LoginRequestDTO("wrong@example.com", "wrongpass");

                Mockito.when(authUseCase.login(Mockito.any(LoginRequestDTO.class)))
                                .thenThrow(new IllegalArgumentException("User not found"));

                mockMvc.perform(post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.error").value("Unauthorized"))
                                .andExpect(jsonPath("$.message").value("User not found"));
        }

        @Test
        void resetPasswordRequest_ShouldReturnSuccessMessage() throws Exception {
                doNothing().when(authUseCase).resetPasswordRequest("john@example.com");

                mockMvc.perform(post("/reset-password-request")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString("john@example.com")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Password reset request successful"))
                                .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        void resetPasswordRequest_ShouldReturnBadRequest_WhenEmailNotFound() throws Exception {
                Mockito.doThrow(new IllegalArgumentException("No user found with email: john@example.com"))
                                .when(authUseCase).resetPasswordRequest("john@example.com");

                mockMvc.perform(post("/reset-password-request")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString("john@example.com")))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.error").value("Unauthorized"))
                                .andExpect(jsonPath("$.message").value("No user found with email: john@example.com"));
        }

        @Test
        void resetPassword_ShouldReturnSuccessMessage() throws Exception {
                doNothing().when(authUseCase).resetPassword("token123", "newPass");

                mockMvc.perform(post("/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("token", "token123", "newPassword", "newPass"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Password reset successful"))
                                .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        void resetPassword_ShouldReturnBadRequest_WhenTokenInvalid() throws Exception {
                Mockito.doThrow(new IllegalArgumentException("Invalid or expired token"))
                                .when(authUseCase).resetPassword("bad-token", "newPass");

                mockMvc.perform(post("/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("token", "bad-token", "newPassword", "newPass"))))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.error").value("Unauthorized"))
                                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
        }

        @Test
        void registerPassenger_ShouldReturnSuccess_WhenRegistrationSucceeds() throws Exception {
                RegisterRequestDTO request = new RegisterRequestDTO("Jane Doe", "jane@example.com", "pass123");
                LoginResponseDTO response = new LoginResponseDTO("jwt-token", 1L, "Jane Doe", "jane@example.com",
                                "PASSENGER");

                Mockito.when(authUseCase.registerPassenger(Mockito.any(RegisterRequestDTO.class))).thenReturn(response);

                mockMvc.perform(post("/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message").value("Registration successful"))
                                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                                .andExpect(jsonPath("$.data.email").value("jane@example.com"))
                                .andExpect(jsonPath("$.data.role").value("PASSENGER"));
        }

        @Test
        void registerPassenger_ShouldHandleValidationError() throws Exception {
                RegisterRequestDTO invalidRequest = new RegisterRequestDTO("", "", ""); // Campos nulos
                Mockito.when(authUseCase.registerPassenger(invalidRequest))
                                .thenThrow(new IllegalArgumentException("Validation failed"));

                mockMvc.perform(post("/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        @Test
        void loginPassenger_ShouldReturnLoginResponseDTO_WhenCredentialsAreValid() throws Exception {
                LoginRequestDTO request = new LoginRequestDTO("passenger@example.com", "securePass");
                LoginResponseDTO response = new LoginResponseDTO("passenger-token", 2L, "Passenger",
                                "passenger@example.com", "PASSENGER");

                Mockito.when(authUseCase.loginPassenger(Mockito.any(LoginRequestDTO.class))).thenReturn(response);

                mockMvc.perform(post("/login-passenger")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Passenger login successful"))
                                .andExpect(jsonPath("$.data.accessToken").value("passenger-token"))
                                .andExpect(jsonPath("$.data.email").value("passenger@example.com"))
                                .andExpect(jsonPath("$.data.role").value("PASSENGER"));
        }

        @Test
        void loginPassenger_ShouldReturnUnauthorized_WhenLoginFails() throws Exception {
                LoginRequestDTO request = new LoginRequestDTO("wrong@example.com", "badpass");

                Mockito.when(authUseCase.loginPassenger(Mockito.any(LoginRequestDTO.class)))
                                .thenThrow(new IllegalArgumentException("Passenger not found"));

                mockMvc.perform(post("/login-passenger")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.error").value("Unauthorized"))
                                .andExpect(jsonPath("$.message").value("Passenger not found"));
        }

}
