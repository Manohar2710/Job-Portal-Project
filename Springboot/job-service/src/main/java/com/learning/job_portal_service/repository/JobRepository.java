package com.learning.job_portal_service.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.learning.job_portal_service.entity.Job;
import com.learning.job_portal_service.enums.ExperienceLevel;
import com.learning.job_portal_service.enums.JobStatus;
import com.learning.job_portal_service.enums.JobType;

public interface JobRepository extends JpaRepository<Job, Long> {

    /**
     * Filtered, paginated list used by GET /api/jobs.
     * All parameters are optional — pass null to skip that filter.
     * The partial index on (created_at DESC) WHERE status='OPEN' is used
     * automatically when statusFilter = 'OPEN'.
     */
    @Query("""
            SELECT j FROM Job j
            WHERE (:status       IS NULL OR j.status          = :status)
              AND (:location     IS NULL OR LOWER(j.location)  LIKE LOWER(CONCAT('%', :location, '%')))
              AND (:companyName  IS NULL OR LOWER(j.companyName) LIKE LOWER(CONCAT('%', :companyName, '%')))
              AND (:jobType      IS NULL OR j.jobType          = :jobType)
              AND (:expLevel     IS NULL OR j.experienceLevel  = :expLevel)
              AND (:remoteOnly   IS NULL OR j.remoteAllowed    = :remoteOnly)
              AND (:salaryMin    IS NULL OR j.salaryMin       >= :salaryMin)
              AND (:salaryMax    IS NULL OR j.salaryMax       <= :salaryMax)
              AND (:keyword      IS NULL OR LOWER(j.title)     LIKE LOWER(CONCAT('%', :keyword, '%'))
                                        OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY j.createdAt DESC
            """)
    Page<Job> search(
            @Param("status")      JobStatus status,
            @Param("location")    String location,
            @Param("companyName") String companyName,
            @Param("jobType")     JobType jobType,
            @Param("expLevel")    ExperienceLevel expLevel,
            @Param("remoteOnly")  Boolean remoteOnly,
            @Param("salaryMin")   java.math.BigDecimal salaryMin,
            @Param("salaryMax")   java.math.BigDecimal salaryMax,
            @Param("keyword")     String keyword,
            Pageable pageable
    );

    /** All jobs posted by a specific recruiter. */
    Page<Job> findByPostedByOrderByCreatedAtDesc(Long postedBy, Pageable pageable);

    /** Increment view counter atomically — avoids loading the full entity. */
    @Modifying
    @Query("UPDATE Job j SET j.viewCount = j.viewCount + 1 WHERE j.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
