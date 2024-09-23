package com.fpivetti.microservices.core.review;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestApplicationConfiguration {
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresSQLContainer() {
        return new PostgreSQLContainer<>("postgres:latest").withStartupTimeoutSeconds(300).withReuse(false);
    }
}
