package com.learning.job_portal_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learning.job_portal_service.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
    
}
