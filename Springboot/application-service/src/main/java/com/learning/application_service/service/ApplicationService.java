package com.learning.application_service.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learning.application_service.audit.entity.ApplicationAuditLog;
import com.learning.application_service.audit.repository.ApplicationAuditLogRepository;
import com.learning.application_service.dto.ApplicationRequest;
import com.learning.application_service.dto.ApplicationResponse;
import com.learning.application_service.dto.StatusUpdateRequest;
import com.learning.application_service.entity.Application;
import com.learning.application_service.enums.ApplicationStatus;
import com.learning.application_service.kafka.KafkaApplicationEventPublisher;
import com.learning.application_service.kafka.event.ApplicationEvent;
import com.learning.application_service.mapper.ApplicationMapper;
import com.learning.application_service.repository.ApplicationRepository;
import com.learning.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository          applicationRepository;
    private final ApplicationAuditLogRepository  auditLogRepository;
    private final KafkaApplicationEventPublisher kafkaPublisher;
    private final ApplicationMapper              applicationMapper;

    // -----------------------------------------------------------------------
    // Seeker operations
    // -----------------------------------------------------------------------

    @Transactional
    public ApplicationResponse apply(ApplicationRequest req) {
        Long userId = requireCurrentUserId();

        if (applicationRepository.existsByJobIdAndApplicantUserId(req.jobId(), userId)) {
            throw new IllegalStateException("You have already applied to this job.");
        }

        // Use mapper to convert request → entity, then set server-managed fields
        Application app = applicationMapper.toEntity(req);
        app.setApplicantUserId(userId);

        Application saved = applicationRepository.save(app);
        log.info("Application created id={} userId={} jobId={}", saved.getId(), userId, req.jobId());

        // ── Async Kafka event — notify notification-service ──────────────
        // Published after the entity is persisted; Kafka failure does NOT roll back the save.
        // recruiterUserId is not available here without a cross-service lookup;
        // notification-service can enrich this from its own data if needed.
        kafkaPublisher.publish(new ApplicationEvent(
                "APPLICATION_RECEIVED",
                saved.getId(),
                saved.getJobId(),
                userId,
                null,        // recruiterUserId — enriched by notification-service
                null,        // oldStatus
                saved.getStatus().name(),
                LocalDateTime.now()
        ), String.valueOf(saved.getId()));

        return applicationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplications(int page, int size) {
        Long userId = requireCurrentUserId();
        return applicationRepository
                .findByApplicantUserIdOrderByAppliedAtDesc(userId, PageRequest.of(page, size))
                .map(applicationMapper::toResponse);
    }

    // -----------------------------------------------------------------------
    // Recruiter operations
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsForJob(Long jobId, int page, int size) {
        log.info("Fetching applications for jobId={}", jobId);
        return applicationRepository
                .findByJobIdOrderByAppliedAtDesc(jobId, PageRequest.of(page, size))
                .map(applicationMapper::toResponse);
    }

    @Transactional("primaryTransactionManager")
    public ApplicationResponse updateStatus(Long applicationId, StatusUpdateRequest req) {
        Application app = findOrThrow(applicationId);
        ApplicationStatus oldStatus = app.getStatus();
        log.info("Updating application id={} status {} -> {}", applicationId, oldStatus, req.status());
        app.setStatus(req.status());
        ApplicationResponse response = applicationMapper.toResponse(applicationRepository.save(app));

        // Write audit record to secondary (MySQL) datasource
        writeAuditLog(applicationId, oldStatus, req.status());

        // ── Async Kafka event — notify applicant of status change ─────────
        kafkaPublisher.publish(new ApplicationEvent(
                "APPLICATION_STATUS_CHANGED",
                applicationId,
                app.getJobId(),
                app.getApplicantUserId(),
                null,
                oldStatus.name(),
                req.status().name(),
                LocalDateTime.now()
        ), String.valueOf(applicationId));

        return response;
    }

    // -----------------------------------------------------------------------
    // Shared
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ApplicationResponse getById(Long id) {
        return applicationMapper.toResponse(findOrThrow(id));
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Application findOrThrow(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
    }

    // -----------------------------------------------------------------------
    // Audit helper — writes to MySQL via secondaryTransactionManager
    // -----------------------------------------------------------------------

    /**
     * Persists a status-change audit record to MySQL.
     * Uses its own transaction on the secondary datasource so a MySQL failure
     * does NOT roll back the primary (PostgreSQL) status update.
     */
    @Transactional("secondaryTransactionManager")
    public void writeAuditLog(Long applicationId,
                               ApplicationStatus oldStatus,
                               ApplicationStatus newStatus) {
        try {
            Long userId = requireCurrentUserId();
            ApplicationAuditLog entry = new ApplicationAuditLog(applicationId, userId, oldStatus, newStatus);
            auditLogRepository.save(entry);
            log.info("Audit log written: applicationId={} {} -> {} by userId={}",
                    applicationId, oldStatus, newStatus, userId);
        } catch (Exception ex) {
            // MySQL being unavailable must not roll back the primary PostgreSQL status update
            log.warn("Failed to write audit log for applicationId={}: {}", applicationId, ex.getMessage());
        }
    }

    private Long requireCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Cannot parse user-id from principal: " + auth.getName());
        }
    }
}
