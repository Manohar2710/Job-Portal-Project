package com.learning.application_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.learning.application_service",   // main service + config + audit subpackages
    "com.learning.common",
    "com.learning.security"               // security-module beans (JwtService, filters, etc.)
})
public class ApplicationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationServiceApplication.class, args);
    }
}
