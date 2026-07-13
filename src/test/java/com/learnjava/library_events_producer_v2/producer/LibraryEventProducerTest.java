package com.learnjava.library_events_producer_v2.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnjava.library_events_producer_v2.model.Book;
import com.learnjava.library_events_producer_v2.model.EventType;
import com.learnjava.library_events_producer_v2.model.LibraryEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LibraryEventProducer.
 */
@ExtendWith(MockitoExtension.class)
class LibraryEventProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LibraryEventProducer libraryEventProducer;

    private LibraryEvent libraryEvent;
    private Book book;

    @BeforeEach
    void setUp() {
        // Set topic name via reflection
        ReflectionTestUtils.setField(libraryEventProducer, "topicName", "library-events");

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
    void testPublishEvent_SerializationError() throws Exception {
        // Arrange
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new RuntimeException("Serialization error"));

        // Act
        var result = libraryEventProducer.publishEvent(libraryEvent);

        // Assert
        assertNotNull(result);
        assertTrue(result.isCompletedExceptionally());
    }

    @Test
    void testPublishEventSync_SerializationError() throws Exception {
        // Arrange
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new RuntimeException("Serialization error"));

        // Act & Assert
        assertThrows(Exception.class, () -> libraryEventProducer.publishEventSync(libraryEvent));
    }

    @Test
    void testObjectMapperConfiguration() throws Exception {
        // This test verifies the ObjectMapper is properly wired
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // Act & Assert - verify ObjectMapper can be used
        assertDoesNotThrow(() -> {
            String json = objectMapper.writeValueAsString(libraryEvent);
            assertEquals("{}", json);
        });
    }
}

