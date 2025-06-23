package com.gtu.auth_service.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "Auth Service API", version = "1.0", description = "API documentation for the Auth Service"),
    servers = {
        @Server(url = "${SWAGGER_SERVER_URL:http://localhost/api/auth}", description = "Server URL")
    }
)
public class OpenAPIConfig {
}
