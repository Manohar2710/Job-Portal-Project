package com.learning.application_service.dto;

import com.learning.application_service.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "status is required")
        ApplicationStatus status
) {}
