package com.learnjava.library_events_producer_v2.service;

import com.learnjava.library_events_producer_v2.exception.LibraryEventException;
import com.learnjava.library_events_producer_v2.model.EventType;
import com.learnjava.library_events_producer_v2.model.LibraryEvent;
import com.learnjava.library_events_producer_v2.producer.LibraryEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service layer for library event operations.
 * Handles business logic, validation, and coordination with Kafka producer.
 */
@Slf4j
@Service
public class LibraryEventService {

    private final LibraryEventProducer libraryEventProducer;

    @Autowired
    public LibraryEventService(LibraryEventProducer libraryEventProducer) {
        this.libraryEventProducer = libraryEventProducer;
    }

    /**
     * Create a new library event and publish to Kafka.
     *
     * @param libraryEvent the library event to create
     * @return the created library event with timestamp and status
     * @throws LibraryEventException if creation fails
     */
    public LibraryEvent createLibraryEvent(LibraryEvent libraryEvent) {
        EventType type = libraryEvent.getEventType();

        log.info("Creating new library event with ID: {} and event type: {}",
                libraryEvent.getLibraryEventId(), type);

        try {
            // Validate event type
            if (type != EventType.ADD &&
                type != EventType.UPDATE) {
                throw new LibraryEventException(
                        "Invalid event type. Must be ADD or UPDATE",
                        "INVALID_REQUEST"
                );
            }

            // For ADD events, ensure event type is correctly set
            if (type == null) {
                throw new LibraryEventException(
                        "Event type is required",
                        "INVALID_REQUEST"
                );
            }

            // Initialize timestamp
            libraryEvent.initializeTimestamp();

            // Publish to Kafka (synchronously)
            libraryEventProducer.publishEventSync(libraryEvent);

            log.info("Library event created successfully with ID: {}",
                    libraryEvent.getLibraryEventId());

            return libraryEvent;

        } catch (LibraryEventException ex) {
            log.error("Library event creation failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during library event creation: {}", ex.getMessage(), ex);
            throw new LibraryEventException(
                    "Failed to create library event: " + ex.getMessage(),
                    "INTERNAL_ERROR",
                    ex
            );
        }
    }

    /**
     * Update an existing library event and publish to Kafka.
     *
     * @param libraryEventId the ID of the library event to update
     * @param libraryEvent   the updated library event data
     * @return the updated library event with timestamp and status
     * @throws LibraryEventException if update fails or event not found
     */
    public LibraryEvent updateLibraryEvent(Long libraryEventId, LibraryEvent libraryEvent) {
        EventType type = libraryEvent.getEventType();
        log.info("Updating library event with ID: {} and event type: {}",
                libraryEventId, type);

        try {
            // Validate that the ID in URL matches the request body
            if (!libraryEventId.equals(libraryEvent.getLibraryEventId())) {
                throw new LibraryEventException(
                        "Library Event ID in URL must match the ID in request body",
                        "INVALID_REQUEST"
                );
            }

            // Validate event type
            if (type == null) {
                throw new LibraryEventException(
                        "Event type is required for update",
                        "INVALID_REQUEST"
                );
            }

            if (type != EventType.ADD &&
                type != EventType.UPDATE) {
                throw new LibraryEventException(
                        "Invalid event type. Must be ADD or UPDATE",
                        "INVALID_REQUEST"
                );
            }

            // Initialize timestamp
            libraryEvent.initializeTimestamp();

            // Publish update to Kafka (synchronously)
            libraryEventProducer.publishEventSync(libraryEvent);

            log.info("Library event updated successfully with ID: {}", libraryEventId);

            return libraryEvent;

        } catch (LibraryEventException ex) {
            log.error("Library event update failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during library event update: {}", ex.getMessage(), ex);
            throw new LibraryEventException(
                    "Failed to update library event: " + ex.getMessage(),
                    "INTERNAL_ERROR",
                    ex
            );
        }
    }

    /**
     * Validate library event data.
     * Additional business logic validation beyond bean validation.
     *
     * @param libraryEvent the library event to validate
     * @throws LibraryEventException if validation fails
     */
    public void validateLibraryEvent(LibraryEvent libraryEvent) {
        if (libraryEvent == null) {
            throw new LibraryEventException(
                    "Library event cannot be null",
                    "INVALID_REQUEST"
            );
        }

        if (libraryEvent.getLibraryEventId() == null || libraryEvent.getLibraryEventId() <= 0) {
            throw new LibraryEventException(
                    "Library Event ID must be a positive number",
                    "INVALID_REQUEST"
            );
        }

        if (libraryEvent.getEventType() == null) {
            throw new LibraryEventException(
                    "Event type is required",
                    "INVALID_REQUEST"
            );
        }

        if (libraryEvent.getBook() == null) {
            throw new LibraryEventException(
                    "Book details are required",
                    "INVALID_REQUEST"
            );
        }

        if (libraryEvent.getBook().getBookId() == null || libraryEvent.getBook().getBookId() <= 0) {
            throw new LibraryEventException(
                    "Book ID must be a positive number",
                    "INVALID_REQUEST"
            );
        }

        if (libraryEvent.getBook().getBookName() == null ||
            libraryEvent.getBook().getBookName().isBlank()) {
            throw new LibraryEventException(
                    "Book Name is required",
                    "INVALID_REQUEST"
            );
        }

        if (libraryEvent.getBook().getBookAuthor() == null ||
            libraryEvent.getBook().getBookAuthor().isBlank()) {
            throw new LibraryEventException(
                    "Book Author is required",
                    "INVALID_REQUEST"
            );
        }
    }
}

