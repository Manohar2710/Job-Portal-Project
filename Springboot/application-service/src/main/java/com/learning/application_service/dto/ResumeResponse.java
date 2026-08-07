package com.learning.application_service.dto;

import java.time.LocalDateTime;

public record ResumeResponse(
        Long id,
        String originalFilename,
        String s3Key,
        LocalDateTime uploadedAt
) {}
