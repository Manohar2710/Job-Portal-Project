package com.learning.application_service.kafka.event;

import java.time.LocalDateTime;

/**
 * Outbound Kafka event published by application-service.
 *
 * eventType values:
 *   APPLICATION_RECEIVED        — new application submitted by a job seeker
 *   APPLICATION_STATUS_CHANGED  — recruiter moved the application to a new status
 */
public record ApplicationEvent(
        String        eventType,
        Long          applicationId,
        Long          jobId,
        Long          applicantUserId,
        Long          recruiterUserId,   // populated for APPLICATION_RECEIVED; null for status changes
        String        oldStatus,
        String        newStatus,
        LocalDateTime occurredAt
) {}
