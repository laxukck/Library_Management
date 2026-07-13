package com.learnjava.library_events_producer_v2.producer;

import com.learnjava.library_events_producer_v2.model.LibraryEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Kafka producer for publishing library events to Kafka topic.
 * Handles asynchronous publishing with callbacks for success/failure.
 */
@Slf4j
@Component
public class LibraryEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.name:library-events}")
    private String topicName;

    @Value("${kafka.producer.publish-timeout-seconds:5}")
    private long publishTimeoutSeconds;

    @Autowired
    public LibraryEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish library event to Kafka topic asynchronously.
     *
     * @param libraryEvent the library event to publish
     * @return CompletableFuture that completes when the publish operation finishes
     */
    public CompletableFuture<Void> publishEvent(LibraryEvent libraryEvent) {
        try {
            String eventKey = String.valueOf(libraryEvent.getLibraryEventId());
            String eventValue = objectMapper.writeValueAsString(libraryEvent);

            log.info("Publishing library event with ID: {} to topic: {}",
                    libraryEvent.getLibraryEventId(), topicName);

            Message<String> message = MessageBuilder
                    .withPayload(eventValue)
                    .setHeader(KafkaHeaders.TOPIC, topicName)
                    .setHeader("kafka_messageKey", eventKey)
                    .build();

            CompletableFuture<Void> future = new CompletableFuture<>();

            kafkaTemplate.send(message).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish library event with ID: {}. Error: {}",
                            libraryEvent.getLibraryEventId(), ex.getMessage(), ex);
                    libraryEvent.setStatus("FAILED");
                    future.completeExceptionally(ex);
                } else {
                    log.info("Successfully published library event with ID: {} to partition: {} with offset: {}",
                            libraryEvent.getLibraryEventId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    libraryEvent.setStatus("PUBLISHED");
                    future.complete(null);
                }
            });

            return future;

        } catch (Exception ex) {
            log.error("Error serializing library event: {}", ex.getMessage(), ex);
            return CompletableFuture.failedFuture(ex);
        }
    }

    /**
     * Publish library event to Kafka topic synchronously.
     * This method blocks until the publish operation completes.
     *
     * @param libraryEvent the library event to publish
     * @throws Exception if publishing fails
     */
    public void publishEventSync(LibraryEvent libraryEvent) throws Exception {
        try {
            String eventKey = String.valueOf(libraryEvent.getLibraryEventId());
            String eventValue = objectMapper.writeValueAsString(libraryEvent);

            log.info("Publishing library event (SYNC) with ID: {} to topic: {}",
                    libraryEvent.getLibraryEventId(), topicName);

            Message<String> message = MessageBuilder
                    .withPayload(eventValue)
                    .setHeader(KafkaHeaders.TOPIC, topicName)
                    .setHeader("kafka_messageKey", eventKey)
                    .build();

            var sendResult = kafkaTemplate.send(message).get(publishTimeoutSeconds, TimeUnit.SECONDS);

            log.info("Successfully published library event (SYNC) with ID: {} to partition: {} with offset: {}",
                    libraryEvent.getLibraryEventId(),
                    sendResult.getRecordMetadata().partition(),
                    sendResult.getRecordMetadata().offset());

            libraryEvent.setStatus("PUBLISHED");

        } catch (TimeoutException ex) {
            log.error("Timed out after {} seconds while publishing library event with ID: {}. Kafka may be unavailable.",
                    publishTimeoutSeconds, libraryEvent.getLibraryEventId(), ex);
            libraryEvent.setStatus("FAILED");
            throw new IllegalStateException(
                    "Kafka broker is unavailable or not reachable at the configured bootstrap server. " +
                            "Please start Kafka and retry.",
                    ex
            );
        } catch (Exception ex) {
            log.error("Error publishing library event (SYNC): {}", ex.getMessage(), ex);
            libraryEvent.setStatus("FAILED");
            throw ex;
        }
    }
}

