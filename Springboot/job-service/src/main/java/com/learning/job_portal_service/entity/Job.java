package com.learning.job_portal_service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.learning.job_portal_service.enums.ExperienceLevel;
import com.learning.job_portal_service.enums.JobStatus;
import com.learning.job_portal_service.enums.JobType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "job")
@Data
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(nullable = false)
    private BigDecimal salaryMin;

    @Column(nullable = false)
    private BigDecimal salaryMax;

    /** user_id of the recruiter who posted this job */
    @Column(name = "posted_by")
    private Long postedBy;

    @Column(name = "company_name")
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type")
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level")
    private ExperienceLevel experienceLevel;

    @Column(name = "remote_allowed", nullable = false)
    private boolean remoteAllowed = false;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "view_count", nullable = false)
    private long viewCount = 0L;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JobSkill> skills = new ArrayList<>();
}
