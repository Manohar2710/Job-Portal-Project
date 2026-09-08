package com.learning.notification_service.dto;

import com.learning.notification_service.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound DTO for directly creating a notification via the REST API.
 * Used by other services (e.g. job-service via Feign) for immediate/admin notifications.
 *
 * High-volume notifications travel through Kafka instead — this endpoint is for
 * targeted, low-frequency use cases.
 */
public record CreateNotificationRequest(

        @NotNull(message = "recipientUserId is required")
        Long recipientUserId,

        @NotNull(message = "type is required")
        NotificationType type,

        @NotBlank(message = "title is required")
        @Size(max = 255)
        String title,

        @Size(max = 3000)
        String message,

        Long referenceId
) {}
