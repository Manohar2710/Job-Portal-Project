package com.learning.application_service.audit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learning.application_service.audit.entity.ApplicationAuditLog;

/**
 * Repository for ApplicationAuditLog — bound to the secondary (MySQL) datasource
 * via SecondaryDataSourceConfig's @EnableJpaRepositories.
 */
public interface ApplicationAuditLogRepository extends JpaRepository<ApplicationAuditLog, Long> {

    /** Fetch the full status-change history for a given application. */
    List<ApplicationAuditLog> findByApplicationIdOrderByChangedAtDesc(Long applicationId);
}
