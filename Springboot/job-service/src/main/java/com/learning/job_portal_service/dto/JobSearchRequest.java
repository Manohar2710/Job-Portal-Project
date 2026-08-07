package com.learning.job_portal_service.dto;

import java.math.BigDecimal;

import com.learning.job_portal_service.enums.ExperienceLevel;
import com.learning.job_portal_service.enums.JobStatus;
import com.learning.job_portal_service.enums.JobType;

/**
 * All fields are optional. Null means "no filter on this field".
 * Bound from query-params by the controller.
 */
public record JobSearchRequest(
        String keyword,
        JobStatus status,
        String location,
        String companyName,
        JobType jobType,
        ExperienceLevel experienceLevel,
        Boolean remoteOnly,
        BigDecimal salaryMin,
        BigDecimal salaryMax
) {}
