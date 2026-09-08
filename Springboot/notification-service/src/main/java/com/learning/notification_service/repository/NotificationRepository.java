package com.learning.notification_service.repository;

import com.learning.notification_service.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** All notifications for a user, newest first (paginated). */
    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** Only unread notifications for a user, newest first. */
    Page<Notification> findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** Count of unread notifications — used for the UI badge counter. */
    long countByRecipientUserIdAndReadFalse(Long userId);

    /**
     * Bulk mark-all-read in a single UPDATE rather than loading and saving each entity.
     * Returns the number of rows affected.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipientUserId = :userId AND n.read = false")
    int markAllReadForUser(@Param("userId") Long userId);
}
