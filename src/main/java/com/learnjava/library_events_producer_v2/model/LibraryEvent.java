package com.learnjava.library_events_producer_v2.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * LibraryEvent entity representing a library event for ADD/UPDATE operations.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LibraryEvent {

    @Positive(message = "Library Event ID must be a positive number")
    private Long libraryEventId;

    @NotNull(message = "Event Type is required")
    private EventType eventType;

    @NotNull(message = "Book details are required")
    @Valid
    private Book book;

    private LocalDateTime timestamp;

    private String status;

    /**
     * Initialize timestamp to current time if not already set.
     */
    public void initializeTimestamp() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}

