package com.learning.job_portal_service.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Outbound Kafka event published by job-service when a job is created, updated, or closed.
 *
 * eventType values:
 *   JOB_POSTED  — new job published by a recruiter
 *   JOB_UPDATED — existing job fields changed
 *   JOB_CLOSED  — job status moved to CLOSED or ARCHIVED
 */
public record JobEvent(
        String        eventType,
        Long          jobId,
        String        title,
        String        companyName,
        String        location,
        BigDecimal    salaryMin,
        BigDecimal    salaryMax,
        Long          postedBy,
        LocalDateTime occurredAt
) {}
