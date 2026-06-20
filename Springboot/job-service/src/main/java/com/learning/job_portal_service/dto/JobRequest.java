package com.learning.job_portal_service.dto;

import java.math.BigDecimal;
import com.learning.job_portal_service.enums.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record JobRequest (
    @NotBlank(message = "Job title is required and cannot be blank")
    String title,
    @NotBlank String description,
    @NotBlank String location,
    @NotNull JobStatus status,
    @NotNull BigDecimal salaryMin,
    @NotNull BigDecimal salaryMax
) {}
