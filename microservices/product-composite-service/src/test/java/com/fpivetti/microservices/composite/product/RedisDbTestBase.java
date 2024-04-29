package com.fpivetti.microservices.composite.product;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class RedisDbTestBase {

    @ServiceConnection
    private static final RedisContainer database = new RedisContainer("redis:latest");

    @BeforeAll
    static void beforeAll() {
        database.start();
    }

    @AfterAll
    static void afterAll() {
        database.stop();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("redis.host", database::getHost);
        registry.add("redis.port", database::getFirstMappedPort);
    }
}
