CREATE TABLE job (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    salary_min DECIMAL(19,2) NOT NULL,
    salary_max DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);