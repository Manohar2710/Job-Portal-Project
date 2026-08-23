package com.learning.security_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Standalone auth service — exposes /api/auth/** (login, register, refresh, logout).
 *
 * scanBasePackages must cover both root packages:
 *   - com.learning.security_service — PersistenceConfig and this main class
 *   - com.learning.security         — AuthController, AuthService, UserDetailsServiceImpl,
 *                                     AuthBeanConfig, entities, repositories
 *
 * Without the explicit scanBasePackages, @SpringBootApplication only scans
 * com.learning.security_service.* (its own package). The classes in
 * com.learning.security.* are never picked up — Spring falls back to
 * inMemoryUserDetailsManager and every login returns 401.
 *
 * The security-module beans (JwtService, JwtAuthenticationFilter, SecurityConfig, CORS)
 * are registered separately via SecurityModuleAutoConfiguration (auto-configuration
 * picked up from META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports).
 */
@SpringBootApplication(scanBasePackages = {
    "com.learning.security_service",
    "com.learning.security"
})
public class SecurityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurityServiceApplication.class, args);
    }
}
