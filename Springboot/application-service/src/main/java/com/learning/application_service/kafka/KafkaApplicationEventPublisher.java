package com.learning.application_service.kafka;

import com.learning.application_service.kafka.event.ApplicationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Thin publisher wrapper around {@link KafkaTemplate} for application domain events.
 *
 * Sends events asynchronously and logs success/failure.
 * The caller (ApplicationService) does not block —
 * a Kafka failure is logged but does NOT roll back the primary DB transaction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaApplicationEventPublisher {

    private final KafkaTemplate<String, ApplicationEvent> kafkaTemplate;

    @Value("${app.kafka.topics.application-events}")
    private String applicationEventsTopic;

    /**
     * Publishes an application domain event to Kafka.
     *
     * @param event the domain event to publish
     * @param key   partition key — typically {@code String.valueOf(applicationId)}
     */
    public void publish(ApplicationEvent event, String key) {
        CompletableFuture<SendResult<String, ApplicationEvent>> future =
                kafkaTemplate.send(applicationEventsTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish ApplicationEvent type={} appId={} to topic={}: {}",
                        event.eventType(), event.applicationId(), applicationEventsTopic, ex.getMessage());
            } else {
                log.debug("Published ApplicationEvent type={} appId={} to {}@offset={}",
                        event.eventType(), event.applicationId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
