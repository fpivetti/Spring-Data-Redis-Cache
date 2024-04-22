package com.fpivetti.microservices.core.review;


import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PostgresTestBase {

    @ServiceConnection
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:latest");

    static {
        database.start();
    }
}