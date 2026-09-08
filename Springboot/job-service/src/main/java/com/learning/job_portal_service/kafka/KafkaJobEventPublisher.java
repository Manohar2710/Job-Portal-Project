package com.learning.job_portal_service.kafka;

import com.learning.job_portal_service.kafka.event.JobEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Thin publisher wrapper around {@link KafkaTemplate}.
 *
 * Sends events asynchronously and logs success/failure via the
 * {@link CompletableFuture} callback. The caller (JobService) does not block —
 * a Kafka failure is logged as an error but does NOT roll back the primary
 * database transaction that has already committed.
 *
 * This ensures at-least-once semantics: a committed job will always produce
 * an event; a Kafka outage causes log noise but not data corruption.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaJobEventPublisher {

    private final KafkaTemplate<String, JobEvent> kafkaTemplate;

    @Value("${app.kafka.topics.job-events}")
    private String jobEventsTopic;

    /**
     * Publishes a job domain event to Kafka asynchronously.
     *
     * @param event the domain event to publish
     * @param key   Kafka partition key — typically {@code String.valueOf(jobId)}
     *              so all events for one job land on the same partition (ordered)
     */
    public void publish(JobEvent event, String key) {
        CompletableFuture<SendResult<String, JobEvent>> future =
                kafkaTemplate.send(jobEventsTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish JobEvent type={} jobId={} to topic={}: {}",
                        event.eventType(), event.jobId(), jobEventsTopic, ex.getMessage());
            } else {
                log.debug("Published JobEvent type={} jobId={} to {}@offset={}",
                        event.eventType(), event.jobId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
