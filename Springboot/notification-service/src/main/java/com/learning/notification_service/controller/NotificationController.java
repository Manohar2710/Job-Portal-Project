package com.learning.notification_service.controller;

import com.learning.notification_service.dto.CreateNotificationRequest;
import com.learning.notification_service.dto.NotificationResponse;
import com.learning.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for notification resources.
 *
 * Endpoints:
 *   GET  /api/notifications/my              — paginated list for authenticated user
 *   GET  /api/notifications/my/unread       — unread notifications only
 *   GET  /api/notifications/my/count        — unread badge count
 *   PATCH /api/notifications/{id}/read      — mark single notification as read
 *   PATCH /api/notifications/my/read-all    — mark all notifications as read
 *   POST  /api/notifications                — internal create (Feign from job-service / admin)
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification management")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get all notifications for the authenticated user (paginated)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {

        return ResponseEntity.ok(notificationService.getForUser(parseUserId(auth), page, size));
    }

    @Operation(summary = "Get unread notifications for the authenticated user")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my/unread")
    public ResponseEntity<Page<NotificationResponse>> getMyUnread(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {

        return ResponseEntity.ok(notificationService.getUnreadForUser(parseUserId(auth), page, size));
    }

    @Operation(summary = "Count unread notifications (badge counter)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my/count")
    public ResponseEntity<Map<String, Long>> countUnread(Authentication auth) {
        return ResponseEntity.ok(Map.of("unread", notificationService.countUnread(parseUserId(auth))));
    }

    @Operation(summary = "Mark a single notification as read")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markRead(id));
    }

    @Operation(summary = "Mark all notifications as read for the authenticated user")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/my/read-all")
    public ResponseEntity<Map<String, Integer>> markAllRead(Authentication auth) {
        return ResponseEntity.ok(
                Map.of("marked", notificationService.markAllRead(parseUserId(auth))));
    }

    /**
     * Internal endpoint — called synchronously by other services via Feign.
     * Any authenticated principal may call this (the JWT is forwarded by the
     * Feign RequestInterceptor in the calling service).
     */
    @Operation(summary = "Create a notification directly (internal service-to-service)")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody CreateNotificationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.create(req));
    }

    // ── Private helper ─────────────────────────────────────────────────────────

    private Long parseUserId(Authentication auth) {
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Cannot parse userId from principal: " + auth.getName());
        }
    }
}
