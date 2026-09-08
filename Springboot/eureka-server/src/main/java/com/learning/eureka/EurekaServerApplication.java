package com.learning.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Discovery Server.
 *
 * All microservices register themselves here on startup using the
 * {@code @EnableDiscoveryClient} annotation on their main class.
 * Service-to-service calls resolve logical names (lb://service-name)
 * via the Spring Cloud LoadBalancer backed by this registry.
 *
 * Dashboard: http://localhost:8761
 * Registered services: http://localhost:8761/eureka/apps
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
