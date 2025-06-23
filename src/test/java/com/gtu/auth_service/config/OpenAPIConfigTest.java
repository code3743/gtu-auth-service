package com.gtu.auth_service.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class OpenAPIConfigTest {

    @Test
    void contextLoads() {
        OpenAPIConfig config = new OpenAPIConfig();
        assertThat(config).isNotNull();
    }
}
