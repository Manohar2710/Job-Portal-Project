package com.learning.application_service.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.learning.application_service.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /** All applications submitted by a specific job seeker. */
    Page<Application> findByApplicantUserIdOrderByAppliedAtDesc(Long applicantUserId, Pageable pageable);

    /** All applications for a specific job — used by recruiter view. */
    Page<Application> findByJobIdOrderByAppliedAtDesc(Long jobId, Pageable pageable);

    /** Prevents duplicate applications from the same user for the same job. */
    boolean existsByJobIdAndApplicantUserId(Long jobId, Long applicantUserId);

    /** Load a specific application for a user (ownership check). */
    Optional<Application> findByIdAndApplicantUserId(Long id, Long applicantUserId);
}
