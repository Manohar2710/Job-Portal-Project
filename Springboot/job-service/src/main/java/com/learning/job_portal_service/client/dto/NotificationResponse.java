package com.learning.job_portal_service.client.dto;

import java.time.LocalDateTime;

/**
 * Inbound DTO received from notification-service via Feign after creating a notification.
 */
public record NotificationResponse(
        Long          id,
        Long          recipientUserId,
        String        type,
        String        title,
        String        message,
        Long          referenceId,
        boolean       read,
        LocalDateTime createdAt
) {}
