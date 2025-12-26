package com.frostetsky.cloudstorage.integration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;


@TestConfiguration
public class TestConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:17");
    }

    @Bean
    public MinIOContainer minIOContainer() {
        return new MinIOContainer("minio/minio:latest")
                .withExposedPorts(9000)
                .withEnv("MINIO_ROOT_USER", "minio_admin")
                .withEnv("MINIO_ROOT_PASSWORD", "minio_admin");
    }

    @Bean
    public DynamicPropertyRegistrar minioPropertyRegistrar(MinIOContainer minio) {
        return registry -> {
            registry.add("minio.url", () -> "http://localhost:" + minio.getMappedPort(9000));
            registry.add("minio.access-key", () -> "minio_admin");
            registry.add("minio.secret-key", () -> "minio_admin");
        };
    }
}
