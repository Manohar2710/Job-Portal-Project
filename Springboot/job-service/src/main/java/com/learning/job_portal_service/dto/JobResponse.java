package com.learning.job_portal_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.learning.job_portal_service.enums.ExperienceLevel;
import com.learning.job_portal_service.enums.JobStatus;
import com.learning.job_portal_service.enums.JobType;

public record JobResponse(
        Long id,
        String title,
        String description,
        String location,
        JobStatus status,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        Long postedBy,
        String companyName,
        JobType jobType,
        ExperienceLevel experienceLevel,
        boolean remoteAllowed,
        LocalDateTime expiresAt,
        long viewCount,
        List<String> skills,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
