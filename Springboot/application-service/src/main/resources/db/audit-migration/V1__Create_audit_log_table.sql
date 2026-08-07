-- MySQL migration: audit log table for application status changes

CREATE TABLE IF NOT EXISTS application_audit_logs (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    application_id      BIGINT       NOT NULL,
    changed_by_user_id  BIGINT       NOT NULL,
    old_status          VARCHAR(50)  NOT NULL,
    new_status          VARCHAR(50)  NOT NULL,
    changed_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_audit_application_id (application_id),
    INDEX idx_audit_changed_at     (changed_at),
    INDEX idx_audit_changed_by     (changed_by_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
