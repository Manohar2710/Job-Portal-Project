package com.learning.notification_service.kafka.event;

import java.time.LocalDateTime;

/**
 * Inbound Kafka event produced by application-service.
 *
 * eventType values:
 *   APPLICATION_RECEIVED        — new application submitted by a seeker
 *   APPLICATION_STATUS_CHANGED  — recruiter moved application to a new status
 */
public record ApplicationEvent(
        String        eventType,
        Long          applicationId,
        Long          jobId,
        Long          applicantUserId,
        Long          recruiterUserId,   // populated for APPLICATION_RECEIVED; null otherwise
        String        oldStatus,
        String        newStatus,
        LocalDateTime occurredAt
) {}
