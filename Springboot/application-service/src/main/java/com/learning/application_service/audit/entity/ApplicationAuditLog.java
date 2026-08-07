package com.learning.application_service.audit.entity;

import java.time.LocalDateTime;

import com.learning.application_service.enums.ApplicationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Audit record written to the secondary MySQL datasource every time an
 * application's status is changed by a recruiter.
 *
 * Persisted via secondaryEntityManagerFactory / secondaryTransactionManager.
 */
@Entity
@Table(name = "application_audit_logs")
@Data
@NoArgsConstructor
public class ApplicationAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The application whose status changed. */
    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    /** The recruiter or admin who made the change. */
    @Column(name = "changed_by_user_id", nullable = false)
    private Long changedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false, length = 50)
    private ApplicationStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 50)
    private ApplicationStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    public ApplicationAuditLog(Long applicationId,
                                Long changedByUserId,
                                ApplicationStatus oldStatus,
                                ApplicationStatus newStatus) {
        this.applicationId   = applicationId;
        this.changedByUserId = changedByUserId;
        this.oldStatus       = oldStatus;
        this.newStatus       = newStatus;
    }
}
