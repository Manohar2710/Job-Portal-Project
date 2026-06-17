package com.learning.job_portal_service.service;

import org.springframework.stereotype.Service;

import com.learning.job_portal_service.dto.JobRequest;
import com.learning.job_portal_service.dto.JobResponse;
import com.learning.job_portal_service.entity.Job;
import com.learning.job_portal_service.repository.JobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

	public JobResponse create(JobRequest jobRequest) {
        
        Job job = new Job();
        job.setTitle(jobRequest.title());
        job.setDescription(jobRequest.description());
        job.setLocation(jobRequest.location());
        job.setSalaryMin(jobRequest.salaryMin());
        job.setSalaryMax(jobRequest.salaryMax());
        job.setStatus(jobRequest.status());

        Job savedJob = jobRepository.save(job);
        return map(savedJob);
	}

    private JobResponse map(Job savedJob) {
        return new JobResponse(
            savedJob.getId(),
            savedJob.getTitle(),
            savedJob.getDescription(),
            savedJob.getLocation(),
            savedJob.getStatus(),
            savedJob.getSalaryMin(),
            savedJob.getSalaryMax(),
            savedJob.getCreatedAt(),
            savedJob.getUpdatedAt()
        );
    }

}
