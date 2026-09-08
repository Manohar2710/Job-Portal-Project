package com.learning.notification_service.kafka;

import com.learning.notification_service.kafka.event.ApplicationEvent;
import com.learning.notification_service.kafka.event.JobEvent;
import com.learning.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer — listens on both {@code job-events} and {@code application-events} topics.
 *
 * Each listener method runs on a thread from the consumer group pool.
 * Errors are caught and logged; the offset is still committed so a single bad message
 * does not block the partition indefinitely.
 *
 * In production, configure a dead-letter topic (DLT) for messages that repeatedly fail.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventListener {

    private final NotificationService notificationService;

    // ── Job events ─────────────────────────────────────────────────────────────

    @KafkaListener(
        topics           = "${app.kafka.topics.job-events}",
        groupId          = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onJobEvent(
            @Payload  JobEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC)     String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int    partition,
            @Header(KafkaHeaders.OFFSET)             long   offset) {

        log.info("Received JobEvent type={} jobId={} topic={} partition={} offset={}",
                event.eventType(), event.jobId(), topic, partition, offset);
        try {
            notificationService.handleJobEvent(event);
        } catch (Exception ex) {
            log.error("Failed processing JobEvent jobId={}: {}", event.jobId(), ex.getMessage(), ex);
        }
    }

    // ── Application events ─────────────────────────────────────────────────────

    @KafkaListener(
        topics           = "${app.kafka.topics.application-events}",
        groupId          = "${spring.kafka.consumer.group-id}",
        containerFactory = "applicationEventListenerContainerFactory"
    )
    public void onApplicationEvent(
            @Payload  ApplicationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC)     String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int    partition,
            @Header(KafkaHeaders.OFFSET)             long   offset) {

        log.info("Received ApplicationEvent type={} appId={} topic={} partition={} offset={}",
                event.eventType(), event.applicationId(), topic, partition, offset);
        try {
            notificationService.handleApplicationEvent(event);
        } catch (Exception ex) {
            log.error("Failed processing ApplicationEvent appId={}: {}",
                    event.applicationId(), ex.getMessage(), ex);
        }
    }
}
