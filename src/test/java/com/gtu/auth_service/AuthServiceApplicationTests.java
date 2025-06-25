package com.gtu.auth_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "reset.links.base=http://localhost:8080/reset-password",
})
@TestPropertySource(properties = "JWT_SECRET=test-secret")
class AuthServiceApplicationTests {
    @Test
    void contextLoads() {
        // This test ensures that the Spring context is loaded correctly.
    }
}

