package com.learning.application_service.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learning.application_service.dto.ApplicationRequest;
import com.learning.application_service.dto.ApplicationResponse;
import com.learning.application_service.dto.ResumeResponse;
import com.learning.application_service.dto.StatusUpdateRequest;
import com.learning.application_service.entity.Application;
import com.learning.application_service.entity.Resume;
import com.learning.application_service.repository.ApplicationRepository;
import com.learning.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    // -----------------------------------------------------------------------
    // Seeker operations
    // -----------------------------------------------------------------------

    @Transactional
    public ApplicationResponse apply(ApplicationRequest req) {
        Long userId = requireCurrentUserId();

        if (applicationRepository.existsByJobIdAndApplicantUserId(req.jobId(), userId)) {
            throw new IllegalStateException("You have already applied to this job.");
        }

        Application app = new Application();
        app.setJobId(req.jobId());
        app.setApplicantUserId(userId);
        app.setCoverLetter(req.coverLetter());

        Application saved = applicationRepository.save(app);
        log.info("Application created id={} userId={} jobId={}", saved.getId(), userId, req.jobId());
        return map(saved);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplications(int page, int size) {
        Long userId = requireCurrentUserId();
        return applicationRepository
                .findByApplicantUserIdOrderByAppliedAtDesc(userId, PageRequest.of(page, size))
                .map(this::map);
    }

    // -----------------------------------------------------------------------
    // Recruiter operations
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsForJob(Long jobId, int page, int size) {
        log.info("Fetching applications for jobId={}", jobId);
        return applicationRepository
                .findByJobIdOrderByAppliedAtDesc(jobId, PageRequest.of(page, size))
                .map(this::map);
    }

    @Transactional
    public ApplicationResponse updateStatus(Long applicationId, StatusUpdateRequest req) {
        Application app = findOrThrow(applicationId);
        log.info("Updating application id={} status={}", applicationId, req.status());
        app.setStatus(req.status());
        return map(applicationRepository.save(app));
    }

    // -----------------------------------------------------------------------
    // Shared
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ApplicationResponse getById(Long id) {
        return map(findOrThrow(id));
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Application findOrThrow(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
    }

    private ApplicationResponse map(Application app) {
        List<ResumeResponse> resumes = app.getResumes() == null
                ? List.of()
                : app.getResumes().stream()
                     .map(r -> new ResumeResponse(r.getId(), r.getOriginalFilename(),
                                                  r.getS3Key(), r.getUploadedAt()))
                     .toList();

        return new ApplicationResponse(
                app.getId(),
                app.getJobId(),
                app.getApplicantUserId(),
                app.getStatus(),
                app.getCoverLetter(),
                resumes,
                app.getAppliedAt(),
                app.getUpdatedAt()
        );
    }

    private Long requireCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Cannot parse user-id from principal: " + auth.getName());
        }
    }
}
