package com.learning.job_portal_service.service;

import java.util.Collections;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learning.common.exception.ResourceNotFoundException;
import com.learning.job_portal_service.dto.JobRequest;
import com.learning.job_portal_service.dto.JobResponse;
import com.learning.job_portal_service.dto.JobSearchRequest;
import com.learning.job_portal_service.entity.Job;
import com.learning.job_portal_service.entity.JobSkill;
import com.learning.job_portal_service.repository.JobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    // -----------------------------------------------------------------------
    // Write operations
    // -----------------------------------------------------------------------

    @Transactional
    @CacheEvict(cacheNames = {"jobs-search", "jobs-mine"}, allEntries = true)
    public JobResponse create(JobRequest req) {
        log.info("Creating job: '{}'", req.title());

        Long currentUserId = resolveCurrentUserId();

        Job job = new Job();
        applyRequest(job, req);
        job.setPostedBy(currentUserId);
        applySkills(job, req.skills());

        Job saved = jobRepository.save(job);
        log.info("Job created, id={}", saved.getId());
        return map(saved);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "job",         key = "#id"),
        @CacheEvict(cacheNames = "jobs-search", allEntries = true),
        @CacheEvict(cacheNames = "jobs-mine",   allEntries = true)
    })
    public JobResponse update(Long id, JobRequest req) {
        log.info("Updating job id={}", id);
        Job job = findOrThrow(id);
        applyRequest(job, req);

        // Replace skill list
        job.getSkills().clear();
        applySkills(job, req.skills());

        Job saved = jobRepository.save(job);
        log.info("Job updated, id={}", id);
        return map(saved);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "job",         key = "#id"),
        @CacheEvict(cacheNames = "jobs-search", allEntries = true),
        @CacheEvict(cacheNames = "jobs-mine",   allEntries = true)
    })
    public void delete(Long id) {
        log.info("Deleting job id={}", id);
        jobRepository.delete(findOrThrow(id));
        log.info("Job deleted, id={}", id);
    }

    // -----------------------------------------------------------------------
    // Read operations
    // -----------------------------------------------------------------------

    @Transactional
    @Cacheable(cacheNames = "job", key = "#id")
    public JobResponse getById(Long id) {
        log.info("Fetching job id={}", id);
        jobRepository.incrementViewCount(id);
        return map(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "jobs-search",
               key = "#req.toString() + '_p' + #page + '_s' + #size")
    public Page<JobResponse> search(JobSearchRequest req, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        log.debug("Job search: {}", req);

        return jobRepository.search(
                req.status(),
                req.location(),
                req.companyName(),
                req.jobType(),
                req.experienceLevel(),
                req.remoteOnly(),
                req.salaryMin(),
                req.salaryMax(),
                req.keyword(),
                pageable
        ).map(this::map);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "jobs-mine",
               key = "@jobService.resolveCurrentUserId() + '_p' + #page + '_s' + #size")
    public Page<JobResponse> getMyJobs(int page, int size) {
        Long userId = resolveCurrentUserId();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return jobRepository.findByPostedByOrderByCreatedAtDesc(userId, pageable).map(this::map);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void applyRequest(Job job, JobRequest req) {
        job.setTitle(req.title());
        job.setDescription(req.description());
        job.setLocation(req.location());
        job.setStatus(req.status());
        job.setSalaryMin(req.salaryMin());
        job.setSalaryMax(req.salaryMax());
        job.setCompanyName(req.companyName());
        job.setJobType(req.jobType());
        job.setExperienceLevel(req.experienceLevel());
        job.setRemoteAllowed(req.remoteAllowed());
        job.setExpiresAt(req.expiresAt());
    }

    private void applySkills(Job job, List<String> skills) {
        if (skills == null) return;
        skills.stream()
              .filter(s -> s != null && !s.isBlank())
              .map(s -> new JobSkill(job, s.trim().toLowerCase()))
              .forEach(job.getSkills()::add);
    }

    private Job findOrThrow(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    private JobResponse map(Job job) {
        List<String> skills = job.getSkills() == null
                ? Collections.emptyList()
                : job.getSkills().stream().map(JobSkill::getSkill).toList();

        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getStatus(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getPostedBy(),
                job.getCompanyName(),
                job.getJobType(),
                job.getExperienceLevel(),
                job.isRemoteAllowed(),
                job.getExpiresAt(),
                job.getViewCount(),
                skills,
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }

    /** Extracts the numeric user-id stored as the JWT subject. */
    private Long resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            log.warn("Could not parse user-id from principal '{}'", auth.getName());
            return null;
        }
    }
}
