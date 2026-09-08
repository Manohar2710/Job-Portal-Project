package com.learning.notification_service.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inbound Kafka event produced by job-service.
 *
 * The JSON payload is deserialized by Spring Kafka's {@code JsonDeserializer}.
 * Field names must match the JSON keys produced by the publisher exactly.
 *
 * eventType values: JOB_POSTED | JOB_UPDATED | JOB_CLOSED
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
