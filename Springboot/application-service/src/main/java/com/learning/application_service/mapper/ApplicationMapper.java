package com.learning.application_service.mapper;

import com.learning.application_service.dto.ApplicationRequest;
import com.learning.application_service.dto.ApplicationResponse;
import com.learning.application_service.dto.ResumeResponse;
import com.learning.application_service.entity.Application;
import com.learning.application_service.entity.Resume;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link Application} ↔ DTO conversions.
 *
 * The MapStruct annotation processor generates {@code ApplicationMapperImpl} at
 * compile time in {@code target/generated-sources/annotations/}.
 * This replaces the private {@code map()} method that previously lived in
 * {@code ApplicationService}, removing all manual field-by-field mapping code.
 *
 * Field mapping notes:
 *   - {@link Application} → {@link ApplicationResponse}: all fields match directly;
 *     the {@code resumes} list is mapped element-by-element via {@code toResumeResponse()}.
 *   - {@link ApplicationRequest} → {@link Application}: several fields are set by the
 *     server after mapping ({@code applicantUserId} from SecurityContext, {@code status}
 *     defaults to APPLIED, timestamps are managed by JPA).
 */
@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    /**
     * Maps an {@link Application} entity to an {@link ApplicationResponse} DTO.
     * The {@code resumes} nested list is converted via {@code toResumeResponse()}.
     */
    ApplicationResponse toResponse(Application application);

    /**
     * Maps a {@link Resume} entity to a {@link ResumeResponse} DTO.
     * Called implicitly by MapStruct when mapping the {@code resumes} collection.
     */
    ResumeResponse toResumeResponse(Resume resume);

    /**
     * Maps an {@link ApplicationRequest} DTO to a new {@link Application} entity.
     * Server-managed and security-context fields are excluded from the mapping.
     */
    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "applicantUserId",  ignore = true)  // set from SecurityContext
    @Mapping(target = "status",           ignore = true)  // defaults to APPLIED
    @Mapping(target = "appliedAt",        ignore = true)  // set by @CreationTimestamp
    @Mapping(target = "updatedAt",        ignore = true)  // set by @UpdateTimestamp
    @Mapping(target = "resumes",          ignore = true)  // managed separately
    @Mapping(target = "notes",            ignore = true)  // managed separately
    Application toEntity(ApplicationRequest request);
}
