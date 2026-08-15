package com.learning.security_service.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Registers the JPA entities and repositories that live in security-service.
 * These are no longer in the shared security-module — they are owned by this service.
 */
@Configuration
@EntityScan(basePackages = "com.learning.security.entity")
@EnableJpaRepositories(basePackages = "com.learning.security.repository")
public class PersistenceConfig {
}
