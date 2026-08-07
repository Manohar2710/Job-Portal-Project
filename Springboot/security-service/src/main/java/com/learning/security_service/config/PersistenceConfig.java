package com.learning.security_service.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Registers the JPA entities and repositories from the security-module so that
 * UserRepository (and RefreshTokenRepository) are available as Spring beans.
 *
 * SecurityModuleAutoConfiguration covers @ComponentScan("com.learning.security"),
 * but JPA repositories need an explicit @EnableJpaRepositories to be discovered
 * when the owning package is outside the application's own scanBasePackages.
 */
@Configuration
@EntityScan(basePackages = "com.learning.security.entity")
@EnableJpaRepositories(basePackages = "com.learning.security.repository")
public class PersistenceConfig {
}
