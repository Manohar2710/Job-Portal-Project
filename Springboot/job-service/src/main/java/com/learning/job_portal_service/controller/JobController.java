package com.learning.job_portal_service.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.job_portal_service.dto.JobRequest;
import com.learning.job_portal_service.dto.JobResponse;
import com.learning.job_portal_service.dto.JobSearchRequest;
import com.learning.job_portal_service.enums.ExperienceLevel;
import com.learning.job_portal_service.enums.JobStatus;
import com.learning.job_portal_service.enums.JobType;
import com.learning.job_portal_service.service.JobService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // -----------------------------------------------------------------------
    // Public / authenticated read endpoints
    // -----------------------------------------------------------------------

    /**
     * GET /api/jobs
     * Paginated, filtered job list.  All query params are optional.
     * Example: GET /api/jobs?keyword=java&location=London&jobType=FULL_TIME&page=0&size=20
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Page<JobResponse>> listJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) Boolean remoteOnly,
            @RequestParam(required = false) BigDecimal salaryMin,
            @RequestParam(required = false) BigDecimal salaryMax,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        JobSearchRequest searchReq = new JobSearchRequest(
                keyword, status, location, companyName,
                jobType, experienceLevel, remoteOnly, salaryMin, salaryMax);

        return ResponseEntity.ok(jobService.search(searchReq, page, size));
    }

    /** GET /api/jobs/{id} — single job detail (increments view count) */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getById(id));
    }

    /** GET /api/jobs/recruiter/mine — all jobs posted by the authenticated recruiter */
    @PreAuthorize("hasAnyAuthority('ROLE_RECRUITER', 'ROLE_ADMIN')")
    @GetMapping("/recruiter/mine")
    public ResponseEntity<Page<JobResponse>> myJobs(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(jobService.getMyJobs(page, size));
    }

    // -----------------------------------------------------------------------
    // Write endpoints (recruiter / admin)
    // -----------------------------------------------------------------------

    /** POST /api/jobs — create a new job */
    @PreAuthorize("hasAnyAuthority('ROLE_RECRUITER', 'ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Validated @RequestBody JobRequest jobRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.create(jobRequest));
    }

    /** PUT /api/jobs/{id} — update an existing job */
    @PreAuthorize("hasAnyAuthority('ROLE_RECRUITER', 'ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Validated @RequestBody JobRequest jobRequest) {
        return ResponseEntity.ok(jobService.update(id, jobRequest));
    }

    /** DELETE /api/jobs/{id} — hard delete (admin only) */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
