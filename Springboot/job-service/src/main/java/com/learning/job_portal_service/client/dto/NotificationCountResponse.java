package com.learning.job_portal_service.client.dto;

/**
 * Inbound DTO received from notification-service for the unread badge count.
 * Matches the Map.of("unread", count) response from NotificationController.
 */
public record NotificationCountResponse(long unread) {}
