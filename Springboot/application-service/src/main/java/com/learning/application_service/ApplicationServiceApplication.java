package com.learning.application_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * application-service — handles job applications, resumes, and audit logs.
 *
 * JWT validation is provided automatically by SecurityModuleAutoConfiguration
 * from the security-module JAR (registered via AutoConfiguration.imports).
 * That auto-config now uses @Import instead of @ComponentScan, so only the six
 * JWT-related beans are registered — no auth controllers, no UserDetailsService,
 * no DB dependencies from the security stack.
 */
@SpringBootApplication(scanBasePackages = {
    "com.learning.application_service",
    "com.learning.common"
})
public class ApplicationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationServiceApplication.class, args);
    }
}
