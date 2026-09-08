package com.learning.notification_service.dto;

import com.learning.notification_service.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * Outbound DTO returned by all REST endpoints.
 * Mirrors the Notification entity but safe to serialize over HTTP.
 */
public record NotificationResponse(
        Long id,
        Long recipientUserId,
        NotificationType type,
        String title,
        String message,
        Long referenceId,
        boolean read,
        LocalDateTime createdAt
) {}
