package com.learning.notification_service.entity;

import com.learning.notification_service.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Persisted notification record.
 *
 * Each row represents a single notification delivered to a specific user.
 * The {@code read} flag is flipped when the user acknowledges it via the REST API
 * or when a bulk mark-all-read operation is performed.
 *
 * Indexes:
 *   - idx_notif_recipient — all queries filter by recipientUserId
 *   - idx_notif_created   — ordering by createdAt DESC
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_recipient", columnList = "recipient_user_id"),
    @Index(name = "idx_notif_created",   columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user this notification is addressed to (matches the JWT subject/userId). */
    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    /** Human-readable title, e.g. "New job posted at Acme Corp". */
    @Column(nullable = false, length = 255)
    private String title;

    /** Full message body (nullable — some notifications are title-only). */
    @Column(columnDefinition = "TEXT")
    private String message;

    /** Optional foreign key to the domain object that triggered this notification (jobId, applicationId). */
    @Column(name = "reference_id")
    private Long referenceId;

    /** Set to true when the user acknowledges the notification. */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
