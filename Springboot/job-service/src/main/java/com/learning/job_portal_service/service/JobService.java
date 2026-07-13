package com.learning.job_portal_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learning.common.exception.ResourceNotFoundException;
import com.learning.job_portal_service.dto.JobRequest;
import com.learning.job_portal_service.dto.JobResponse;
import com.learning.job_portal_service.entity.Job;
import com.learning.job_portal_service.repository.JobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    public JobResponse create(JobRequest jobRequest) {
        log.info("Creating job with title: '{}'", jobRequest.title());

        Job job = new Job();
        job.setTitle(jobRequest.title());
        job.setDescription(jobRequest.description());
        job.setLocation(jobRequest.location());
        job.setSalaryMin(jobRequest.salaryMin());
        job.setSalaryMax(jobRequest.salaryMax());
        job.setStatus(jobRequest.status());

        Job savedJob = jobRepository.save(job);
        log.info("Job created successfully, jobId: {}, title: '{}'", savedJob.getId(), savedJob.getTitle());
        return map(savedJob);
    }

    @Transactional(readOnly = true)
    public JobResponse getById(Long id) {
        log.info("Fetching job with id: {}", id);
        Job job = findOrThrow(id);
        return map(job);
    }

    @Transactional
    public JobResponse update(Long id, JobRequest jobRequest) {
        log.info("Updating job with id: {}", id);
        Job job = findOrThrow(id);

        job.setTitle(jobRequest.title());
        job.setDescription(jobRequest.description());
        job.setLocation(jobRequest.location());
        job.setSalaryMin(jobRequest.salaryMin());
        job.setSalaryMax(jobRequest.salaryMax());
        job.setStatus(jobRequest.status());

        Job savedJob = jobRepository.save(job);
        log.info("Job updated successfully, jobId: {}", savedJob.getId());
        return map(savedJob);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting job with id: {}", id);
        Job job = findOrThrow(id);
        jobRepository.delete(job);
        log.info("Job deleted successfully, jobId: {}", id);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Job findOrThrow(Long id) {
        return jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    private JobResponse map(Job job) {
        return new JobResponse(
            job.getId(),
            job.getTitle(),
            job.getDescription(),
            job.getLocation(),
            job.getStatus(),
            job.getSalaryMin(),
            job.getSalaryMax(),
            job.getCreatedAt(),
            job.getUpdatedAt()
        );
    }

}
