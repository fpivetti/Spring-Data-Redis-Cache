package com.fpivetti.microservices.core.recommendation;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;

public abstract class MongoDbTestBase {

    @ServiceConnection
    private static final MongoDBContainer database = new MongoDBContainer("mongo:latest");

    static {
        database.start();
    }
}
