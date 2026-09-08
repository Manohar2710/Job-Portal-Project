package com.learning.notification_service.service;

import com.learning.common.exception.ResourceNotFoundException;
import com.learning.notification_service.dto.CreateNotificationRequest;
import com.learning.notification_service.dto.NotificationResponse;
import com.learning.notification_service.entity.Notification;
import com.learning.notification_service.enums.NotificationType;
import com.learning.notification_service.kafka.event.ApplicationEvent;
import com.learning.notification_service.kafka.event.JobEvent;
import com.learning.notification_service.mapper.NotificationMapper;
import com.learning.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core notification business logic.
 *
 * Responsibilities:
 *   1. Translate inbound Kafka events into Notification entities and persist them.
 *   2. Serve notification queries via the REST API.
 *   3. Handle read / mark-all-read acknowledgement.
 *
 * MapStruct ({@link NotificationMapper}) is used for all entity ↔ DTO conversions
 * — no manual mapping code in this class.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper     notificationMapper;

    // ── Kafka event handlers ───────────────────────────────────────────────────

    @Transactional
    public void handleJobEvent(JobEvent event) {
        NotificationType type = resolveJobType(event.eventType());

        // Notify the recruiter who owns this job
        if (event.postedBy() == null) {
            log.warn("JobEvent has no postedBy — skipping notification for jobId={}", event.jobId());
            return;
        }

        Notification notification = Notification.builder()
                .recipientUserId(event.postedBy())
                .type(type)
                .title(buildJobTitle(type, event.title(), event.companyName()))
                .message(buildJobMessage(type, event))
                .referenceId(event.jobId())
                .build();

        notificationRepository.save(notification);
        log.info("Persisted {} notification for recruiter userId={} jobId={}",
                type, event.postedBy(), event.jobId());
    }

    @Transactional
    public void handleApplicationEvent(ApplicationEvent event) {
        NotificationType type = resolveApplicationType(event.eventType());

        // APPLICATION_RECEIVED → notify the recruiter
        if ("APPLICATION_RECEIVED".equals(event.eventType()) && event.recruiterUserId() != null) {
            Notification n = Notification.builder()
                    .recipientUserId(event.recruiterUserId())
                    .type(type)
                    .title("New application received for job #" + event.jobId())
                    .message("Applicant (userId=" + event.applicantUserId() + ") has applied.")
                    .referenceId(event.applicationId())
                    .build();
            notificationRepository.save(n);
            log.info("Persisted APPLICATION_RECEIVED notification for recruiter userId={}",
                    event.recruiterUserId());
        }

        // APPLICATION_STATUS_CHANGED → notify the applicant
        if ("APPLICATION_STATUS_CHANGED".equals(event.eventType())) {
            Notification n = Notification.builder()
                    .recipientUserId(event.applicantUserId())
                    .type(type)
                    .title("Your application status has changed")
                    .message("Application #" + event.applicationId()
                            + ": " + event.oldStatus() + " → " + event.newStatus())
                    .referenceId(event.applicationId())
                    .build();
            notificationRepository.save(n);
            log.info("Persisted APPLICATION_STATUS_CHANGED notification for applicant userId={}",
                    event.applicantUserId());
        }
    }

    // ── REST operations ────────────────────────────────────────────────────────

    /**
     * Direct notification creation via REST (called by Feign from job-service or admin tools).
     * Uses MapStruct to convert the request DTO to an entity.
     */
    @Transactional
    public NotificationResponse create(CreateNotificationRequest req) {
        Notification saved = notificationRepository.save(notificationMapper.toEntity(req));
        log.info("Direct notification created id={} for userId={}", saved.getId(), req.recipientUserId());
        return notificationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getForUser(Long userId, int page, int size) {
        return notificationRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadForUser(Long userId, int page, int size) {
        return notificationRepository
                .findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByRecipientUserIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found: " + notificationId));
        n.setRead(true);
        return notificationMapper.toResponse(notificationRepository.save(n));
    }

    @Transactional
    public int markAllRead(Long userId) {
        int count = notificationRepository.markAllReadForUser(userId);
        log.info("Marked {} notifications as read for userId={}", count, userId);
        return count;
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private NotificationType resolveJobType(String eventType) {
        return switch (eventType) {
            case "JOB_POSTED"  -> NotificationType.JOB_POSTED;
            case "JOB_UPDATED" -> NotificationType.JOB_UPDATED;
            case "JOB_CLOSED"  -> NotificationType.JOB_CLOSED;
            default -> {
                log.warn("Unknown job event type: {}", eventType);
                yield NotificationType.JOB_UPDATED;
            }
        };
    }

    private NotificationType resolveApplicationType(String eventType) {
        return switch (eventType) {
            case "APPLICATION_RECEIVED"       -> NotificationType.APPLICATION_RECEIVED;
            case "APPLICATION_STATUS_CHANGED" -> NotificationType.APPLICATION_STATUS_CHANGED;
            default -> {
                log.warn("Unknown application event type: {}", eventType);
                yield NotificationType.APPLICATION_STATUS_CHANGED;
            }
        };
    }

    private String buildJobTitle(NotificationType type, String jobTitle, String company) {
        return switch (type) {
            case JOB_POSTED  -> "Job posted: " + jobTitle + " @ " + company;
            case JOB_UPDATED -> "Job updated: " + jobTitle;
            case JOB_CLOSED  -> "Job closed: "  + jobTitle;
            default          -> jobTitle;
        };
    }

    private String buildJobMessage(NotificationType type, JobEvent event) {
        return switch (type) {
            case JOB_POSTED  -> "Your job '" + event.title() + "' at " + event.companyName()
                    + " in " + event.location() + " is now live.";
            case JOB_UPDATED -> "Job '" + event.title() + "' has been updated.";
            case JOB_CLOSED  -> "Job '" + event.title() + "' has been closed.";
            default          -> "";
        };
    }
}
