package com.learning.application_service.config;

import com.learning.application_service.kafka.event.ApplicationEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka producer configuration for application-service.
 *
 * Key settings:
 *   - acks=all          — leader and all in-sync replicas must acknowledge
 *   - retries=3         — retry transient failures
 *   - enable.idempotence — prevents duplicate messages caused by producer retries
 *   - JsonSerializer    — serializes {@link ApplicationEvent} to JSON;
 *                         adds {@code __TypeId__} header used by the consumer
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, ApplicationEvent> applicationEventProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,   bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG,                "all");
        props.put(ProducerConfig.RETRIES_CONFIG,             3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,  true);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, ApplicationEvent> kafkaTemplate() {
        return new KafkaTemplate<>(applicationEventProducerFactory());
    }
}
