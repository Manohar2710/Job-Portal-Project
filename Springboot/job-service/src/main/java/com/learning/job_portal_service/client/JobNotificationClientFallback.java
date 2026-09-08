package com.learning.job_portal_service.client;

import com.learning.job_portal_service.client.dto.NotificationCountResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for {@link JobNotificationClient}.
 * Returns a zero count when notification-service is unreachable.
 * The job detail response is still returned — just without the badge count.
 */
@Slf4j
@Component
public class JobNotificationClientFallback implements JobNotificationClient {

    @Override
    public NotificationCountResponse getUnreadCount() {
        log.warn("notification-service unavailable — returning zero unread count as fallback");
        return new NotificationCountResponse(0L);
    }
}
