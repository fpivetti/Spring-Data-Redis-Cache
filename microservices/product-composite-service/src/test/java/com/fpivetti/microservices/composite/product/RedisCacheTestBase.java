package com.fpivetti.microservices.composite.product;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class RedisCacheTestBase {

    @ServiceConnection
    private static final RedisContainer cache = new RedisContainer("redis:latest");

    @BeforeAll
    static void beforeAll() {
        cache.start();
    }

    @AfterAll
    static void afterAll() {
        cache.stop();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", cache::getHost);
        registry.add("spring.data.redis.port", cache::getFirstMappedPort);
    }
}
