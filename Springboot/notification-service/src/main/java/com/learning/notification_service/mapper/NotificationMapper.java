package com.learning.notification_service.mapper;

import com.learning.notification_service.dto.CreateNotificationRequest;
import com.learning.notification_service.dto.NotificationResponse;
import com.learning.notification_service.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper — compile-time generated, Spring-managed bean.
 *
 * The MapStruct processor generates {@code NotificationMapperImpl} in
 * {@code target/generated-sources/annotations/} at compile time.
 * No reflection at runtime — pure method delegation.
 *
 * Configuration:
 *   - componentModel = "spring"        → generated class is annotated @Component
 *   - unmappedTargetPolicy = WARN      → build warning (not error) for missing mappings
 *     (configured globally via -Amapstruct.unmappedTargetPolicy in the compiler plugin)
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    /**
     * Entity → response DTO.
     * All field names and types match 1:1 between Notification and NotificationResponse,
     * so no explicit {@code @Mapping} is needed.
     */
    NotificationResponse toResponse(Notification notification);

    /**
     * Create-request → new entity.
     * The server-managed fields ({@code id}, {@code createdAt}, {@code read}) are
     * excluded — they are set by JPA / the {@code @Builder.Default} on the entity.
     */
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "read",      ignore = true)
    Notification toEntity(CreateNotificationRequest request);
}
