package com.learnjava.library_events_producer_v2.controller;

import com.learnjava.library_events_producer_v2.model.Book;
import com.learnjava.library_events_producer_v2.model.EventType;
import com.learnjava.library_events_producer_v2.model.LibraryEvent;
import com.learnjava.library_events_producer_v2.service.LibraryEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LibraryEventController.
 */
@ExtendWith(MockitoExtension.class)
class LibraryEventControllerTest {

    @Mock
    private LibraryEventService libraryEventService;

    @InjectMocks
    private LibraryEventController libraryEventController;

    private LibraryEvent libraryEvent;
    private Book book;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .bookId(101L)
                .bookName("The Art of Computer Programming")
                .bookAuthor("Donald E. Knuth")
                .build();

        libraryEvent = LibraryEvent.builder()
                .libraryEventId(1L)
                .eventType(EventType.ADD)
                .book(book)
                .timestamp(LocalDateTime.now())
                .status("PUBLISHED")
                .build();
    }

    @Test
    void testCreateLibraryEvent_Success() {
        // Arrange
        when(libraryEventService.createLibraryEvent(any(LibraryEvent.class)))
                .thenReturn(libraryEvent);

        // Act
        ResponseEntity<LibraryEvent> response = libraryEventController.createLibraryEvent(libraryEvent);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getLibraryEventId());
        assertEquals(EventType.ADD, response.getBody().getEventType());
        assertEquals("The Art of Computer Programming", response.getBody().getBook().getBookName());
        verify(libraryEventService, times(1)).createLibraryEvent(any(LibraryEvent.class));
    }

    @Test
    void testUpdateLibraryEvent_Success() {
        // Arrange
        libraryEvent.setEventType(EventType.UPDATE);
        libraryEvent.getBook().setBookName("The Art of Computer Programming - 4th Edition");

        when(libraryEventService.updateLibraryEvent(1L, libraryEvent))
                .thenReturn(libraryEvent);

        // Act
        ResponseEntity<LibraryEvent> response = libraryEventController.updateLibraryEvent(1L, libraryEvent);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getLibraryEventId());
        assertEquals(EventType.UPDATE, response.getBody().getEventType());
        assertEquals("The Art of Computer Programming - 4th Edition", response.getBody().getBook().getBookName());
        verify(libraryEventService, times(1)).updateLibraryEvent(1L, libraryEvent);
    }

    @Test
    void testHealthCheck() {
        // Act
        ResponseEntity<String> response = libraryEventController.health();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Library Events Producer is running!", response.getBody());
    }
}

