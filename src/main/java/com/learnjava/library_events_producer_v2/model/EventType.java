package com.learnjava.library_events_producer_v2.model;

/**
 * Enum representing the types of library events.
 */
public enum EventType {
    ADD("Represents a new book addition to the library"),
    UPDATE("Represents an update to existing book information");

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

