package com.learning.job_portal_service.service;

import java.time.LocalDateTime;
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
import com.learning.job_portal_service.client.JobNotificationClient;
import com.learning.job_portal_service.client.dto.NotificationRequest;
import com.learning.job_portal_service.dto.JobRequest;
import com.learning.job_portal_service.dto.JobResponse;
import com.learning.job_portal_service.dto.JobSearchRequest;
import com.learning.job_portal_service.entity.Job;
import com.learning.job_portal_service.entity.JobSkill;
import com.learning.job_portal_service.enums.JobStatus;
import com.learning.job_portal_service.kafka.KafkaJobEventPublisher;
import com.learning.job_portal_service.kafka.event.JobEvent;
import com.learning.job_portal_service.mapper.JobMapper;
import com.learning.job_portal_service.repository.JobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository          jobRepository;
    private final JobMapper              jobMapper;
    private final KafkaJobEventPublisher kafkaPublisher;
    private final JobNotificationClient  notificationClient;

    // -----------------------------------------------------------------------
    // Write operations
    // -----------------------------------------------------------------------

    @Transactional
    @CacheEvict(cacheNames = {"jobs-search", "jobs-mine"}, allEntries = true)
    public JobResponse create(JobRequest req) {
        log.info("Creating job: '{}'", req);

        Long currentUserId = resolveCurrentUserId();
        log.info("currentUserId: '{}'", currentUserId);

        Job job = jobMapper.toEntity(req);

        job.setPostedBy(currentUserId);
        applySkills(job, req.skills());

        Job saved = jobRepository.save(job);
        log.info("Job created id={}", saved);

        // ── Async notification via Kafka ──────────────────────────────────
        // Published after the DB transaction commits; Kafka failure does NOT
        // roll back the job creation.
        kafkaPublisher.publish(buildJobEvent("JOB_POSTED", saved), String.valueOf(saved.getId()));

        // ── Sync notification via Feign (Eureka) ─────────────────────────
        // Best-effort direct call to notification-service via Eureka load-balancer.
        // The fallback handles unavailability gracefully — job creation is unaffected.
        if (currentUserId != null) {
            notificationClient.createNotification(new NotificationRequest(
                    currentUserId,
                    "JOB_POSTED",
                    "Job '" + saved.getTitle() + "' is now live!",
                    "Your job posting at " + saved.getCompanyName() + " is active.",
                    saved.getId()
            ));
        }

        return jobMapper.toResponse(saved);
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

        // Apply scalar fields via mapper (skills managed separately)
        Job updated = jobMapper.toEntity(req);
        job.setTitle(updated.getTitle());
        job.setDescription(updated.getDescription());
        job.setLocation(updated.getLocation());
        job.setStatus(updated.getStatus());
        job.setSalaryMin(updated.getSalaryMin());
        job.setSalaryMax(updated.getSalaryMax());
        job.setCompanyName(updated.getCompanyName());
        job.setJobType(updated.getJobType());
        job.setExperienceLevel(updated.getExperienceLevel());
        job.setRemoteAllowed(updated.isRemoteAllowed());
        job.setExpiresAt(updated.getExpiresAt());

        // Replace skill list
        job.getSkills().clear();
        applySkills(job, req.skills());

        Job saved = jobRepository.save(job);
        log.info("Job updated id={}", id);

        // Determine event type: closing a job emits JOB_CLOSED, other changes emit JOB_UPDATED
        boolean isClosed = saved.getStatus() == JobStatus.CLOSED
                || saved.getStatus() == JobStatus.ARCHIVED;
        String eventType = isClosed ? "JOB_CLOSED" : "JOB_UPDATED";
        kafkaPublisher.publish(buildJobEvent(eventType, saved), String.valueOf(saved.getId()));

        return jobMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(cacheNames = "job",         key = "#id"),
        @CacheEvict(cacheNames = "jobs-search", allEntries = true),
        @CacheEvict(cacheNames = "jobs-mine",   allEntries = true)
    })
    public void delete(Long id) {
        log.info("Deleting job id={}", id);
        Job job = findOrThrow(id);
        jobRepository.delete(job);
        kafkaPublisher.publish(buildJobEvent("JOB_CLOSED", job), String.valueOf(id));
        log.info("Job deleted id={}", id);
    }

    // -----------------------------------------------------------------------
    // Read operations
    // -----------------------------------------------------------------------

    @Transactional
    @Cacheable(cacheNames = "job", key = "#id")
    public JobResponse getById(Long id) {
        log.info("Fetching job id={}", id);
        jobRepository.incrementViewCount(id);
        return jobMapper.toResponse(findOrThrow(id));
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
        ).map(jobMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "jobs-mine",
               key = "@jobService.resolveCurrentUserId() + '_p' + #page + '_s' + #size")
    public Page<JobResponse> getMyJobs(int page, int size) {
        Long userId = resolveCurrentUserId();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return jobRepository.findByPostedByOrderByCreatedAtDesc(userId, pageable)
                .map(jobMapper::toResponse);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

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

    private JobEvent buildJobEvent(String eventType, Job job) {
        return new JobEvent(
                eventType,
                job.getId(),
                job.getTitle(),
                job.getCompanyName(),
                job.getLocation(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getPostedBy(),
                LocalDateTime.now()
        );
    }

    /** Extracts the numeric user-id stored as the JWT subject. */
    public Long resolveCurrentUserId() {
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
