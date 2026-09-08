package com.learning.job_portal_service.client;

import com.learning.job_portal_service.client.dto.NotificationRequest;
import com.learning.job_portal_service.client.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for {@link JobNotificationClient}.
 *
 * Invoked when notification-service is unreachable or returns an error.
 * The fallback logs a warning and returns {@code null} so that the caller
 * (JobService) can treat the direct notification as best-effort and continue
 * without throwing an exception.
 *
 * The async Kafka path is the primary notification mechanism;
 * this synchronous Feign call is supplementary.
 */
@Slf4j
@Component
public class JobNotificationClientFallback implements JobNotificationClient {

    @Override
    public NotificationResponse createNotification(NotificationRequest request) {
        log.warn("notification-service unavailable — fallback triggered for direct notification "
                + "(recipientUserId={}, type={})", request.recipientUserId(), request.type());
        return null;
    }
}
