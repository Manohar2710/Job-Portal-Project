package com.learning.application_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Application Service bootstrap class.
 *
 * JWT validation is provided automatically by SecurityModuleAutoConfiguration
 * from the security-module JAR (registered via AutoConfiguration.imports).
 * That auto-config uses @Import, so only the six JWT-related beans are
 * registered — no auth controllers, no UserDetailsService, no DB dependencies.
 *
 * @EnableDiscoveryClient registers this service with the Eureka server on startup,
 * enabling service-to-service discovery from other microservices.
 */
@SpringBootApplication(scanBasePackages = {
    "com.learning.application_service",
    "com.learning.common"
})
@EnableDiscoveryClient
public class ApplicationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationServiceApplication.class, args);
    }
}
