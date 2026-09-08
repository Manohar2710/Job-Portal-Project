package com.learning.job_portal_service.dto;

/**
 * Enriched job response that combines the job detail with the calling user's
 * unread notification count fetched synchronously from notification-service via Feign.
 *
 * This DTO is returned by GET /api/jobs/{id}/detail and demonstrates the only
 * correct use of synchronous Feign here: the client needs BOTH pieces of data
 * in one response, so the caller must wait for the result.
 */
public record JobWithNotificationCountResponse(
        JobResponse job,
        long unreadNotifications
) {}
