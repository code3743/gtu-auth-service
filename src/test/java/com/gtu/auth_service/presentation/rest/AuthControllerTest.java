package com.gtu.auth_service.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtu.auth_service.application.dto.LoginRequestDTO;
import com.gtu.auth_service.application.dto.LoginResponseDTO;
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
                        .param("email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset request successful"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void resetPasswordRequest_ShouldReturnBadRequest_WhenEmailNotFound() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("No user found with email: john@example.com"))
                .when(authUseCase).resetPasswordRequest("john@example.com");

        mockMvc.perform(post("/reset-password-request")
                        .param("email", "john@example.com"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("No user found with email: john@example.com"));
    }

    @Test
    void resetPassword_ShouldReturnSuccessMessage() throws Exception {
        doNothing().when(authUseCase).resetPassword("token123", "newPass");

        mockMvc.perform(post("/reset-password")
                        .param("token", "token123")
                        .param("newPassword", "newPass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successful"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void resetPassword_ShouldReturnBadRequest_WhenTokenInvalid() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Invalid or expired token"))
                .when(authUseCase).resetPassword("bad-token", "newPass");

        mockMvc.perform(post("/reset-password")
                        .param("token", "bad-token")
                        .param("newPassword", "newPass"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }
}
