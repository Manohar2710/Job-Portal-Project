-- V1: Application Service tables

-- Core applications table
CREATE TABLE IF NOT EXISTS applications (
    id                 BIGSERIAL PRIMARY KEY,
    job_id             BIGINT       NOT NULL,
    applicant_user_id  BIGINT       NOT NULL,
    status             VARCHAR(50)  NOT NULL DEFAULT 'APPLIED',
    cover_letter       TEXT,
    applied_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_application UNIQUE (job_id, applicant_user_id)
);

CREATE INDEX IF NOT EXISTS idx_app_job_id       ON applications(job_id);
CREATE INDEX IF NOT EXISTS idx_app_user_id      ON applications(applicant_user_id);
CREATE INDEX IF NOT EXISTS idx_app_status       ON applications(status);
CREATE INDEX IF NOT EXISTS idx_app_applied_at   ON applications(applied_at DESC);

-- Uploaded resumes
CREATE TABLE IF NOT EXISTS resumes (
    id              BIGSERIAL PRIMARY KEY,
    application_id  BIGINT        NOT NULL,
    s3_key          VARCHAR(500)  NOT NULL,
    original_filename VARCHAR(255),
    uploaded_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_application FOREIGN KEY (application_id)
        REFERENCES applications(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_resume_application ON resumes(application_id);

-- Recruiter internal notes on an application
CREATE TABLE IF NOT EXISTS application_notes (
    id                BIGSERIAL PRIMARY KEY,
    application_id    BIGINT   NOT NULL,
    recruiter_user_id BIGINT   NOT NULL,
    note              TEXT     NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_note_application FOREIGN KEY (application_id)
        REFERENCES applications(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notes_application ON application_notes(application_id);
