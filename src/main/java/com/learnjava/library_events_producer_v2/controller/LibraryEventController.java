package com.learnjava.library_events_producer_v2.controller;

import com.learnjava.library_events_producer_v2.model.LibraryEvent;
import com.learnjava.library_events_producer_v2.service.LibraryEventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for library event endpoints.
 * Exposes POST and PUT endpoints for publishing library events to Kafka.
 */
@Slf4j
@RestController
@RequestMapping("/library-events")
public class LibraryEventController {

    private final LibraryEventService libraryEventService;

    @Autowired
    public LibraryEventController(LibraryEventService libraryEventService) {
        this.libraryEventService = libraryEventService;
    }

    /**
     * Create a new library event and publish to Kafka.
     * HTTP POST /library-events
     *
     * @param libraryEvent the library event to create
     * @return ResponseEntity with status 201 Created and the created event
     */
    @PostMapping
    public ResponseEntity<LibraryEvent> createLibraryEvent(
            @Valid @RequestBody LibraryEvent libraryEvent) {

        log.info("Received POST request to create library event with ID: {} and event type: {}",
                libraryEvent.getLibraryEventId(), libraryEvent.getEventType());

        LibraryEvent createdEvent = libraryEventService.createLibraryEvent(libraryEvent);

        log.info("Successfully created library event with ID: {}", createdEvent.getLibraryEventId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    /**
     * Update an existing library event and publish the update to Kafka.
     * HTTP PUT /library-events/{libraryEventId}
     *
     * @param libraryEventId the ID of the library event to update
     * @param libraryEvent   the updated library event data
     * @return ResponseEntity with status 200 OK and the updated event
     */
    @PutMapping("/{libraryEventId}")
    public ResponseEntity<LibraryEvent> updateLibraryEvent(
            @PathVariable Long libraryEventId,
            @Valid @RequestBody LibraryEvent libraryEvent) {

        log.info("Received PUT request to update library event with ID: {} and event type: {}",
                libraryEventId, libraryEvent.getEventType());

        LibraryEvent updatedEvent = libraryEventService.updateLibraryEvent(libraryEventId, libraryEvent);

        log.info("Successfully updated library event with ID: {}", updatedEvent.getLibraryEventId());

        return ResponseEntity.status(HttpStatus.OK).body(updatedEvent);
    }

    /**
     * Health check endpoint.
     * HTTP GET /library-events/health
     *
     * @return ResponseEntity with status 200 OK
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("Health check endpoint called");
        return ResponseEntity.ok("Library Events Producer is running!");
    }
}

