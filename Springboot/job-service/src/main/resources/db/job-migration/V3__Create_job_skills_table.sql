-- V3: Skills tags per job — used for skill-based filtering

CREATE TABLE IF NOT EXISTS job_skills (
    id       BIGSERIAL PRIMARY KEY,
    job_id   BIGINT NOT NULL,
    skill    VARCHAR(100) NOT NULL,
    CONSTRAINT fk_job_skills_job FOREIGN KEY (job_id)
        REFERENCES job(id) ON DELETE CASCADE,
    CONSTRAINT uq_job_skill UNIQUE (job_id, skill)
);

CREATE INDEX IF NOT EXISTS idx_job_skills_job_id ON job_skills(job_id);
CREATE INDEX IF NOT EXISTS idx_job_skills_skill  ON job_skills(skill);
