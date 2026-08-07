package com.learning.security_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Standalone auth service — exposes /api/auth/** (login, register, refresh, logout).
 * All auth logic lives in the security-module library; this application just provides
 * the runtime host (port 8081) and datasource configuration for it.
 *
 * SecurityModuleAutoConfiguration is picked up automatically via
 * META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 * in the security-module JAR, so the full com.learning.security package
 * (AuthController, SecurityConfig, JwtAuthenticationFilter, etc.) is registered here.
 */
@SpringBootApplication(scanBasePackages = {
    "com.learning.security_service",
    "com.learning.common"
})
public class SecurityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurityServiceApplication.class, args);
    }
}
