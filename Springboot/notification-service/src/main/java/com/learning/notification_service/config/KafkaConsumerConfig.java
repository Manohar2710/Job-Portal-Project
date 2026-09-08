package com.learning.notification_service.config;

import com.learning.notification_service.kafka.event.ApplicationEvent;
import com.learning.notification_service.kafka.event.JobEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration for the notification-service.
 *
 * Design decisions:
 *
 * 1. {@link ErrorHandlingDeserializer} wraps both value deserializers so that a
 *    single malformed JSON message does not kill the consumer thread — deserialization
 *    errors are caught and logged; the offset still advances.
 *
 * 2. {@code ENABLE_AUTO_COMMIT_CONFIG = false} + {@code AckMode.RECORD}:
 *    Offsets are committed only after the listener method returns successfully.
 *    This prevents message loss if the service restarts mid-processing.
 *
 * 3. Two typed {@link ConsumerFactory} beans (one per event type) are defined for
 *    clarity, but the shared {@link ConcurrentKafkaListenerContainerFactory} uses
 *    {@code Object} as the value generic and relies on the {@code __TypeId__} header
 *    injected by the Kafka producer's {@link org.springframework.kafka.support.serializer.JsonSerializer}
 *    to select the correct target type at runtime.
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // ── Shared base properties ─────────────────────────────────────────────────

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        return props;
    }

    // ── Typed consumer factories (used for explicit injection if needed) ────────

    @Bean
    public ConsumerFactory<String, JobEvent> jobEventConsumerFactory() {
        JsonDeserializer<JobEvent> deserializer = new JsonDeserializer<>(JobEvent.class, false);
        deserializer.addTrustedPackages("com.learning.*");
        return new DefaultKafkaConsumerFactory<>(
                baseConsumerProps(),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(deserializer)
        );
    }

    @Bean
    public ConsumerFactory<String, ApplicationEvent> applicationEventConsumerFactory() {
        JsonDeserializer<ApplicationEvent> deserializer =
                new JsonDeserializer<>(ApplicationEvent.class, false);
        deserializer.addTrustedPackages("com.learning.*");
        return new DefaultKafkaConsumerFactory<>(
                baseConsumerProps(),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(deserializer)
        );
    }

    // ── Typed listener container factories (one per event type) ───────────────
    //
    // Using a shared Object-typed factory causes Jackson to deserialize to
    // LinkedHashMap when no __TypeId__ header is present (producers using plain
    // JsonSerializer do not always add it). Separate typed factories guarantee
    // the correct target class is used for each listener regardless of headers.

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, JobEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, JobEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(jobEventConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setConcurrency(2);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ApplicationEvent> applicationEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ApplicationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(applicationEventConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setConcurrency(2);
        return factory;
    }
}
