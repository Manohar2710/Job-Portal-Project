package com.learning.notification_service.enums;

/**
 * Classifies the kind of event that triggered a notification.
 * Stored as a VARCHAR in the notifications table.
 */
public enum NotificationType {
    JOB_POSTED,
    JOB_UPDATED,
    JOB_CLOSED,
    APPLICATION_RECEIVED,
    APPLICATION_STATUS_CHANGED
}
