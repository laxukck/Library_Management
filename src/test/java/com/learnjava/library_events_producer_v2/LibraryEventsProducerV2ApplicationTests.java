package com.learnjava.library_events_producer_v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnjava.library_events_producer_v2.model.Book;
import com.learnjava.library_events_producer_v2.model.EventType;
import com.learnjava.library_events_producer_v2.model.LibraryEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LibraryEventsProducerV2ApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
		// Verify that the Spring context loads successfully
	}

	@Test
	void objectMapperSerializesLibraryEventTimestamp() {
		LibraryEvent libraryEvent = LibraryEvent.builder()
				.libraryEventId(1L)
				.eventType(EventType.ADD)
				.book(Book.builder()
						.bookId(101L)
						.bookName("Clean Code")
						.bookAuthor("Robert C. Martin")
						.build())
				.timestamp(LocalDateTime.of(2026, 7, 13, 2, 0, 34))
				.status("PUBLISHED")
				.build();

		assertDoesNotThrow(() -> {
			String json = objectMapper.writeValueAsString(libraryEvent);
			assertTrue(json.contains("2026-07-13T02:00:34"));
		});
	}

	@Test
	void plainObjectMapperSerializesLibraryEventTimestampViaFieldAnnotations() {
		LibraryEvent libraryEvent = LibraryEvent.builder()
				.libraryEventId(2L)
				.eventType(EventType.ADD)
				.book(Book.builder()
						.bookId(102L)
						.bookName("Effective Java")
						.bookAuthor("Joshua Bloch")
						.build())
				.timestamp(LocalDateTime.of(2026, 7, 13, 2, 16, 41))
				.status("PUBLISHED")
				.build();

		assertDoesNotThrow(() -> {
			String json = new ObjectMapper().writeValueAsString(libraryEvent);
			assertTrue(json.contains("2026-07-13T02:16:41"));
		});
	}

}


