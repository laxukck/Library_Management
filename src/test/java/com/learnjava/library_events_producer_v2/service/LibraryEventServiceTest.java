package com.learnjava.library_events_producer_v2.service;

import com.learnjava.library_events_producer_v2.exception.LibraryEventException;
import com.learnjava.library_events_producer_v2.model.Book;
import com.learnjava.library_events_producer_v2.model.EventType;
import com.learnjava.library_events_producer_v2.model.LibraryEvent;
import com.learnjava.library_events_producer_v2.producer.LibraryEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LibraryEventService.
 */
@ExtendWith(MockitoExtension.class)
class LibraryEventServiceTest {

    @Mock
    private LibraryEventProducer libraryEventProducer;

    @InjectMocks
    private LibraryEventService libraryEventService;

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
                .build();
    }

    @Test
    void testCreateLibraryEvent_Success() throws Exception {
        // Arrange
        doAnswer(invocation -> {
            LibraryEvent event = invocation.getArgument(0);
            event.setStatus("PUBLISHED");
            return null;
        }).when(libraryEventProducer).publishEventSync(any(LibraryEvent.class));

        // Act
        LibraryEvent result = libraryEventService.createLibraryEvent(libraryEvent);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getLibraryEventId());
        assertEquals(EventType.ADD, result.getEventType());
        assertEquals("PUBLISHED", result.getStatus());
        assertNotNull(result.getTimestamp());
        verify(libraryEventProducer, times(1)).publishEventSync(any(LibraryEvent.class));
    }

    @Test
    void testCreateLibraryEvent_NullEventType_ThrowsException() {
        // Arrange
        libraryEvent.setEventType(null);

        // Act & Assert
        assertThrows(LibraryEventException.class, () -> libraryEventService.createLibraryEvent(libraryEvent));
    }

    @Test
    void testUpdateLibraryEvent_Success() throws Exception {
        // Arrange
        libraryEvent.setEventType(EventType.UPDATE);

        doAnswer(invocation -> {
            LibraryEvent event = invocation.getArgument(0);
            event.setStatus("PUBLISHED");
            return null;
        }).when(libraryEventProducer).publishEventSync(any(LibraryEvent.class));

        // Act
        LibraryEvent result = libraryEventService.updateLibraryEvent(1L, libraryEvent);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getLibraryEventId());
        assertEquals(EventType.UPDATE, result.getEventType());
        assertEquals("PUBLISHED", result.getStatus());
        verify(libraryEventProducer, times(1)).publishEventSync(any(LibraryEvent.class));
    }

    @Test
    void testUpdateLibraryEvent_IdMismatch_ThrowsException() {
        // Arrange
        libraryEvent.setEventType(EventType.UPDATE);

        // Act & Assert
        assertThrows(LibraryEventException.class,
                () -> libraryEventService.updateLibraryEvent(2L, libraryEvent));
    }

    @Test
    void testUpdateLibraryEvent_NullEventType_ThrowsException() {
        // Arrange
        libraryEvent.setEventType(null);

        // Act & Assert
        assertThrows(LibraryEventException.class,
                () -> libraryEventService.updateLibraryEvent(1L, libraryEvent));
    }

    @Test
    void testValidateLibraryEvent_ValidEvent_NoException() {
        // Act & Assert
        assertDoesNotThrow(() -> libraryEventService.validateLibraryEvent(libraryEvent));
    }

    @Test
    void testValidateLibraryEvent_NullEvent_ThrowsException() {
        // Act & Assert
        assertThrows(LibraryEventException.class, () -> libraryEventService.validateLibraryEvent(null));
    }

    @Test
    void testValidateLibraryEvent_InvalidLibraryEventId_ThrowsException() {
        // Arrange
        libraryEvent.setLibraryEventId(0L);

        // Act & Assert
        assertThrows(LibraryEventException.class, () -> libraryEventService.validateLibraryEvent(libraryEvent));
    }

    @Test
    void testValidateLibraryEvent_NullEventType_ThrowsException() {
        // Arrange
        libraryEvent.setEventType(null);

        // Act & Assert
        assertThrows(LibraryEventException.class, () -> libraryEventService.validateLibraryEvent(libraryEvent));
    }

    @Test
    void testValidateLibraryEvent_NullBook_ThrowsException() {
        // Arrange
        libraryEvent.setBook(null);

        // Act & Assert
        assertThrows(LibraryEventException.class, () -> libraryEventService.validateLibraryEvent(libraryEvent));
    }

    @Test
    void testValidateLibraryEvent_InvalidBookId_ThrowsException() {
        // Arrange
        book.setBookId(0L);

        // Act & Assert
        assertThrows(LibraryEventException.class, () -> libraryEventService.validateLibraryEvent(libraryEvent));
    }

    @Test
    void testValidateLibraryEvent_BlankBookName_ThrowsException() {
        // Arrange
        book.setBookName("");

        // Act & Assert
        assertThrows(LibraryEventException.class, () -> libraryEventService.validateLibraryEvent(libraryEvent));
    }

    @Test
    void testValidateLibraryEvent_BlankBookAuthor_ThrowsException() {
        // Arrange
        book.setBookAuthor("");

        // Act & Assert
        assertThrows(LibraryEventException.class, () -> libraryEventService.validateLibraryEvent(libraryEvent));
    }
}

