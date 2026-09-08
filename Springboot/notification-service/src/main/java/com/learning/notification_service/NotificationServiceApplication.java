package com.learning.notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Notification Service — persists and serves user notifications.
 *
 * Dual communication patterns demonstrated in this service:
 *
 * 1. Asynchronous (Kafka consumer):
 *    Listens on {@code job-events} and {@code application-events} topics.
 *    For every event, a Notification row is persisted and optionally
 *    fanned-out to email/push stubs.
 *
 * 2. Synchronous (REST via Eureka):
 *    Exposes GET/PATCH endpoints for the UI to read and acknowledge notifications.
 *    Also exposes POST /api/notifications for internal services (called via Feign).
 *
 * Service discovery: registers with Eureka on startup so other services can
 * resolve it by name {@code notification-service} via lb:// URIs.
 */
@SpringBootApplication(scanBasePackages = {
    "com.learning.notification_service",
    "com.learning.common"
})
@EnableDiscoveryClient
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
