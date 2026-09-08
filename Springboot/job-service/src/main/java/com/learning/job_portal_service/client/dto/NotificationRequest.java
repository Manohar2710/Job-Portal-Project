package com.learning.job_portal_service.client.dto;

/**
 * Outbound DTO sent to notification-service via Feign.
 *
 * Field names and types must match the {@code CreateNotificationRequest} record
 * in notification-service exactly so Jackson can serialize/deserialize correctly.
 */
public record NotificationRequest(
        Long   recipientUserId,
        String type,           // NotificationType enum value as string
        String title,
        String message,
        Long   referenceId
) {}
