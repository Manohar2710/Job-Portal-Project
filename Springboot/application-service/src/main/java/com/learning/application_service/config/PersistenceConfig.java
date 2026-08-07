package com.learning.application_service.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Registers JPA entities and repositories from both this service
 * and the security-module (needed so UserRepository is found when
 * SecurityModuleAutoConfiguration wires UserDetailsServiceImpl).
 */
@Configuration
@EntityScan(basePackages = {
    "com.learning.application_service.entity",
    "com.learning.security.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.learning.application_service.repository",
    "com.learning.security.repository"
})
public class PersistenceConfig {
}
