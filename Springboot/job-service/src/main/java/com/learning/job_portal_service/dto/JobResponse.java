package com.learning.job_portal_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.learning.job_portal_service.enums.JobStatus;

public record JobResponse (
    Long id,
    String title,
    String description,
    String location,
    JobStatus status,
    BigDecimal salaryMin,
    BigDecimal salaryMax,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
