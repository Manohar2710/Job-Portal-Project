package com.learning.application_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(
        @NotNull(message = "jobId is required")
        Long jobId,

        @Size(max = 3000, message = "Cover letter must be under 3000 characters")
        String coverLetter
) {}
