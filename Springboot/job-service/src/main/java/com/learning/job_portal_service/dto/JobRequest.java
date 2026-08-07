package com.learning.job_portal_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.learning.job_portal_service.enums.ExperienceLevel;
import com.learning.job_portal_service.enums.JobStatus;
import com.learning.job_portal_service.enums.JobType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JobRequest(
        @NotBlank(message = "Job title is required and cannot be blank")
        @Size(max = 255)
        String title,

        @NotBlank
        @Size(max = 2000)
        String description,

        @NotBlank
        String location,

        @NotNull
        JobStatus status,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false, message = "salaryMin must be positive")
        BigDecimal salaryMin,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false, message = "salaryMax must be positive")
        BigDecimal salaryMax,

        String companyName,

        JobType jobType,

        ExperienceLevel experienceLevel,

        boolean remoteAllowed,

        LocalDateTime expiresAt,

        @Size(max = 20, message = "At most 20 skills per job")
        List<String> skills
) {}
