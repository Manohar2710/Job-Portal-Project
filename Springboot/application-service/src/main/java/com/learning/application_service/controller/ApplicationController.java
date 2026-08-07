package com.learning.application_service.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.application_service.dto.ApplicationRequest;
import com.learning.application_service.dto.ApplicationResponse;
import com.learning.application_service.dto.StatusUpdateRequest;
import com.learning.application_service.service.ApplicationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // -----------------------------------------------------------------------
    // Seeker endpoints
    // -----------------------------------------------------------------------

    /**
     * POST /api/applications
     * Job seeker submits a new application.
     */
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping
    public ResponseEntity<ApplicationResponse> apply(
            @Validated @RequestBody ApplicationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.apply(req));
    }

    /**
     * GET /api/applications/my?page=0&size=20
     * Returns all applications submitted by the authenticated seeker.
     */
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/my")
    public ResponseEntity<Page<ApplicationResponse>> myApplications(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(applicationService.getMyApplications(page, size));
    }

    // -----------------------------------------------------------------------
    // Recruiter endpoints
    // -----------------------------------------------------------------------

    /**
     * GET /api/applications/job/{jobId}?page=0&size=20
     * Returns all applicants for a given job posting.
     */
    @PreAuthorize("hasAnyAuthority('ROLE_RECRUITER', 'ROLE_ADMIN')")
    @GetMapping("/job/{jobId}")
    public ResponseEntity<Page<ApplicationResponse>> applicantsForJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(applicationService.getApplicationsForJob(jobId, page, size));
    }

    /**
     * PATCH /api/applications/{id}/status
     * Recruiter updates application status (VIEWED, SHORTLISTED, REJECTED, HIRED).
     */
    @PreAuthorize("hasAnyAuthority('ROLE_RECRUITER', 'ROLE_ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long id,
            @Validated @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(applicationService.updateStatus(id, req));
    }

    // -----------------------------------------------------------------------
    // Shared
    // -----------------------------------------------------------------------

    /**
     * GET /api/applications/{id}
     * Fetch a single application by id.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getById(id));
    }
}
