package com.learning.job_portal_service.mapper;

import com.learning.job_portal_service.dto.JobRequest;
import com.learning.job_portal_service.dto.JobResponse;
import com.learning.job_portal_service.entity.Job;
import com.learning.job_portal_service.entity.JobSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

/**
 * MapStruct mapper for {@link Job} ↔ DTO conversions.
 *
 * The MapStruct annotation processor generates {@code JobMapperImpl} at compile time.
 * This replaces the private {@code map()} and {@code applyRequest()} methods
 * that previously lived in {@code JobService}.
 *
 * Key mappings:
 *   - {@code skills} (List<JobSkill>) → {@code skills} (List<String>)
 *     using the {@code @Named} helper {@code skillsToStrings}.
 *   - Server-managed fields ({@code id}, {@code postedBy}, {@code viewCount},
 *     {@code createdAt}, {@code updatedAt}, {@code skills}) are excluded when
 *     mapping request → entity.
 */
@Mapper(componentModel = "spring")
public interface JobMapper {

    /**
     * Maps a {@link Job} entity to a {@link JobResponse} DTO.
     * The {@code skills} field requires a custom conversion from List<JobSkill> to List<String>.
     */
    @Mapping(source = "skills", target = "skills", qualifiedByName = "skillsToStrings")
    JobResponse toResponse(Job job);

    /**
     * Maps a {@link JobRequest} DTO to a new {@link Job} entity.
     * Fields managed by the server or set separately are ignored.
     */
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "postedBy",    ignore = true)  // set from SecurityContext in JobService
    @Mapping(target = "viewCount",   ignore = true)  // DB default = 0
    @Mapping(target = "createdAt",   ignore = true)  // set by @CreationTimestamp
    @Mapping(target = "updatedAt",   ignore = true)  // set by @UpdateTimestamp
    @Mapping(target = "skills",      ignore = true)  // managed separately via applySkills()
    Job toEntity(JobRequest request);

    /** Converts a list of {@link JobSkill} entities to plain skill-name strings. */
    @Named("skillsToStrings")
    default List<String> skillsToStrings(List<JobSkill> skills) {
        if (skills == null) return Collections.emptyList();
        return skills.stream().map(JobSkill::getSkill).toList();
    }
}
