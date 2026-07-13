package com.learnjava.library_events_producer_v2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for producer setup.
 * Provides KafkaTemplate and ObjectMapper beans.
 */
@Configuration
public class KafkaConfig {

    /**
     * Provide ObjectMapper bean for JSON serialization.
     *
     * @return configured ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Additional configuration can be added here if needed
        return objectMapper;
    }

    /**
     * Provide KafkaTemplate bean for sending messages.
     * Uses default Spring Boot configuration from application properties.
     *
     * @param producerFactory the producer factory
     * @return configured KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}

