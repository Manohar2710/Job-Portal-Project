package com.learning.job_portal_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Job Service bootstrap class.
 *
 * Annotations:
 *   @SpringBootApplication — enables component scan, auto-configuration, and Spring configuration.
 *   @EnableDiscoveryClient — registers this service with the Eureka server on startup.
 *   @EnableFeignClients    — scans the client package for @FeignClient interfaces and creates
 *                            proxy beans backed by Spring Cloud LoadBalancer + Eureka.
 */
@SpringBootApplication(scanBasePackages = {
    "com.learning.job_portal_service",
    "com.learning.common"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.learning.job_portal_service.client")
public class JobPortalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobPortalServiceApplication.class, args);
    }
}
