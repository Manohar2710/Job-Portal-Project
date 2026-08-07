package com.learning.application_service.dto;

import com.learning.application_service.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ApplicationResponse(
        Long id,
        Long jobId,
        Long applicantUserId,
        ApplicationStatus status,
        String coverLetter,
        List<ResumeResponse> resumes,
        LocalDateTime appliedAt,
        LocalDateTime updatedAt
) {}
