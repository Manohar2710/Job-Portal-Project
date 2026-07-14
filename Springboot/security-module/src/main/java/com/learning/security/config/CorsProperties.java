package com.learning.security.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Binds the {@code security.cors.*} block from application.yaml so that
 * the CORS policy can be driven entirely by configuration without touching code.
 *
 * <pre>
 * security:
 *   cors:
 *     allowed-origins:
 *       - http://localhost:4200
 *     allowed-methods: [GET, POST, PUT, DELETE, OPTIONS]
 *     allowed-headers: "*"
 *     allow-credentials: true
 *     max-age: 3600
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.cors")
public class CorsProperties {
    private List<String> allowedOrigins = List.of();
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
    private List<String> allowedHeaders = List.of("*");
    private boolean allowCredentials = false;
    private long maxAge = 3600;
}
