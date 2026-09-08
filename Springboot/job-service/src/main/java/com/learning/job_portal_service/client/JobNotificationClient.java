package com.learning.job_portal_service.client;

import com.learning.job_portal_service.client.dto.NotificationRequest;
import com.learning.job_portal_service.client.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * OpenFeign declarative REST client for the notification-service.
 *
 * How service discovery works:
 *   1. {@code name = "notification-service"} is the Eureka application name.
 *   2. Spring Cloud LoadBalancer intercepts the {@code lb://notification-service} URI,
 *      queries the Eureka registry for healthy instances, and selects one (round-robin).
 *   3. Feign serializes the request and sends it to the chosen instance.
 *
 * This demonstrates synchronous service-to-service communication (job-service → notification-service).
 * The Kafka path handles high-volume async notifications; this Feign path is for
 * direct, low-frequency calls (e.g., immediate confirmation after a job is posted).
 *
 * If notification-service is unavailable, {@link JobNotificationClientFallback} is invoked.
 */
@FeignClient(
    name     = "notification-service",
    path     = "/api/notifications",
    fallback = JobNotificationClientFallback.class
)
public interface JobNotificationClient {

    /**
     * Creates a notification directly via REST.
     * Requires the caller to hold ROLE_ADMIN — the JWT token is forwarded by Feign automatically
     * if a {@code RequestInterceptor} propagating the Authorization header is configured.
     */
    @PostMapping
    NotificationResponse createNotification(@RequestBody NotificationRequest request);
}
