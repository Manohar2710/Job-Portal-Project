package com.learning.job_portal_service.client;

import com.learning.job_portal_service.client.dto.NotificationCountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * OpenFeign declarative REST client for the notification-service.
 *
 * Demonstrates genuine synchronous service-to-service communication:
 * job-service needs a value FROM notification-service to include in a response.
 *
 * Discovery flow:
 *   1. {@code name = "notification-service"} is the Eureka application name.
 *   2. Spring Cloud LoadBalancer resolves lb://notification-service → a live instance.
 *   3. The JWT RequestInterceptor (FeignClientConfig) forwards the Bearer token.
 *
 * Rule of thumb applied here:
 *   Sync  (Feign) — when the caller NEEDS the result to complete its own response.
 *   Async (Kafka) — when the caller is notifying something happened (fire-and-forget).
 *
 * Notifications for job events are published via Kafka (async).
 * This client is used only when the response explicitly needs data from
 * notification-service (e.g. unread count badge on the job detail response).
 */
@FeignClient(
    name     = "notification-service",
    path     = "/api/notifications",
    fallback = JobNotificationClientFallback.class
)
public interface JobNotificationClient {

    /**
     * Fetches the unread notification count for the authenticated user.
     * Used to enrich the job listing response with the recruiter's badge count.
     * The caller NEEDS this number synchronously — Kafka cannot serve this.
     */
    @GetMapping("/my/count")
    NotificationCountResponse getUnreadCount();
}
