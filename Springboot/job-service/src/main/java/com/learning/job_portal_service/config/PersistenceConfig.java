package com.learning.job_portal_service.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Registers only this service's own JPA entities and repositories.
 * The User/security entities are owned by security-service — job-service
 * has no dependency on them.
 */
@Configuration
@EntityScan(basePackages = "com.learning.job_portal_service.entity")
@EnableJpaRepositories(basePackages = "com.learning.job_portal_service.repository")
public class PersistenceConfig {
}
