-- V2: Extend job table with fields needed for search, filtering, and recruiter ownership

ALTER TABLE job
    ADD COLUMN IF NOT EXISTS posted_by       BIGINT,
    ADD COLUMN IF NOT EXISTS company_name    VARCHAR(255),
    ADD COLUMN IF NOT EXISTS job_type        VARCHAR(50),
    ADD COLUMN IF NOT EXISTS experience_level VARCHAR(50),
    ADD COLUMN IF NOT EXISTS remote_allowed  BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS expires_at      TIMESTAMP,
    ADD COLUMN IF NOT EXISTS view_count      BIGINT NOT NULL DEFAULT 0;

-- Indexes for the most common filter/sort queries
CREATE INDEX IF NOT EXISTS idx_job_status        ON job(status);
CREATE INDEX IF NOT EXISTS idx_job_location      ON job(location);
CREATE INDEX IF NOT EXISTS idx_job_type          ON job(job_type);
CREATE INDEX IF NOT EXISTS idx_job_company       ON job(company_name);
CREATE INDEX IF NOT EXISTS idx_job_posted_by     ON job(posted_by);
CREATE INDEX IF NOT EXISTS idx_job_expires_at    ON job(expires_at);
CREATE INDEX IF NOT EXISTS idx_job_created_at    ON job(created_at DESC);

-- Partial index: the vast majority of list queries target only OPEN jobs
CREATE INDEX IF NOT EXISTS idx_job_open_created
    ON job(created_at DESC)
    WHERE status = 'OPEN';
