# Implementation Plan
## Library Events Producer v2

**Date:** July 12, 2026  
**Version:** 1.0  

---

## Overview
This document outlines the step-by-step implementation plan for the Library Events Producer v2 Spring Boot application.

---

## Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                    REST API Layer                        │
│  (Controller - POST/PUT endpoints for /library-events)  │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              Service/Business Logic Layer               │
│  (LibraryEventService - validation & business rules)    │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│          Kafka Producer/Publisher Layer                 │
│  (KafkaProducer - publishes events to Kafka)           │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│               Data Model Layer                          │
│  (Entities: Book, LibraryEvent, EventType Enum)        │
└─────────────────────────────────────────────────────────┘
```

---

## Layer-by-Layer Implementation Plan

### Layer 1: Data Models (Entities)
**Files to create:**
- `src/main/java/com/learnjava/library_events_producer_v2/model/Book.java`
- `src/main/java/com/learnjava/library_events_producer_v2/model/LibraryEvent.java`
- `src/main/java/com/learnjava/library_events_producer_v2/model/EventType.java`

**Details:**
- Book: bookId (Long), bookName (String), bookAuthor (String)
- EventType: Enum with ADD, UPDATE values
- LibraryEvent: libraryEventId (Long), eventType (EventType), book (Book), timestamp (LocalDateTime), status (String)
- Add validation annotations (@NotNull, @NotBlank, @Positive, etc.)
- Add Lombok @Data, @AllArgsConstructor, @NoArgsConstructor for boilerplate

---

### Layer 2: Exception Handling
**Files to create:**
- `src/main/java/com/learnjava/library_events_producer_v2/exception/LibraryEventException.java`
- `src/main/java/com/learnjava/library_events_producer_v2/exception/GlobalExceptionHandler.java`
- `src/main/java/com/learnjava/library_events_producer_v2/dto/ErrorResponse.java`

**Details:**
- Custom exception class for library event errors
- Global exception handler for REST advice
- ErrorResponse DTO for standardized error responses
- Handle validation errors (400), resource not found (404), conflicts (409), server errors (500)

---

### Layer 3: Kafka Producer/Publisher
**Files to create:**
- `src/main/java/com/learnjava/library_events_producer_v2/producer/LibraryEventProducer.java`
- `src/main/resources/application.yaml` (update with Kafka configuration)

**Details:**
- KafkaTemplate-based producer
- Publish method for LibraryEvent
- Success/failure callbacks with logging
- Message key based on libraryEventId
- JSON serialization for messages

---

### Layer 4: Service Layer
**Files to create:**
- `src/main/java/com/learnjava/library_events_producer_v2/service/LibraryEventService.java`

**Details:**
- Business logic for event processing
- Validation of incoming data
- Coordination with Kafka producer
- Error handling and logging
- Methods: createLibraryEvent, updateLibraryEvent

---

### Layer 5: Controller/REST API Layer
**Files to create:**
- `src/main/java/com/learnjava/library_events_producer_v2/controller/LibraryEventController.java`

**Details:**
- POST `/library-events` endpoint (201 Created)
- PUT `/library-events/{libraryEventId}` endpoint (200 OK)
- Request validation
- Response entity building with appropriate status codes
- Logging for all requests

---

### Layer 6: Configuration
**Files to update/create:**
- `src/main/resources/application.yaml` (Kafka & server configuration)
- `src/main/java/com/learnjava/library_events_producer_v2/config/KafkaConfig.java` (optional advanced config)

**Details:**
- Kafka bootstrap servers
- Producer serializers (String key, JSON value)
- Topic name configuration
- Server port (8080)
- Logging levels

---

### Layer 7: Testing
**Files to create:**
- `src/test/java/com/learnjava/library_events_producer_v2/controller/LibraryEventControllerTest.java`
- `src/test/java/com/learnjava/library_events_producer_v2/service/LibraryEventServiceTest.java`
- `src/test/java/com/learnjava/library_events_producer_v2/producer/LibraryEventProducerTest.java`

**Details:**
- Unit tests for controller endpoints
- Service layer tests with mocked producer
- Kafka producer integration tests
- Validation tests
- Error handling tests
- Minimum 80% code coverage

---

## Implementation Order

1. ✓ Read PRD and understand requirements
2. → Update build.gradle (add Lombok, add Jackson if needed)
3. → Create Data Models (Book, LibraryEvent, EventType)
4. → Create Exception Handling (Custom exceptions, Global handler, ErrorResponse DTO)
5. → Create Kafka Producer
6. → Create Service Layer
7. → Create Controller/REST API
8. → Update Configuration (application.yaml)
9. → Create Unit and Integration Tests
10. → Test locally with Kafka running
11. → Document API with examples

---

## Key Technologies & Patterns

- **Spring Boot 4** - Web framework
- **Java 25** - Language version
- **Apache Kafka** - Message broker
- **Spring Kafka** - Kafka integration
- **Jakarta Bean Validation** - Input validation
- **Lombok** - Reduce boilerplate
- **JUnit 5** - Testing framework
- **Mockito** - Mocking in tests

---

## File Structure

```
src/main/java/com/learnjava/library_events_producer_v2/
├── LibraryEventsProducerV2Application.java (main)
├── controller/
│   └── LibraryEventController.java
├── service/
│   └── LibraryEventService.java
├── producer/
│   └── LibraryEventProducer.java
├── model/
│   ├── Book.java
│   ├── LibraryEvent.java
│   └── EventType.java
├── exception/
│   ├── LibraryEventException.java
│   └── GlobalExceptionHandler.java
├── dto/
│   └── ErrorResponse.java
└── config/
    └── KafkaConfig.java (optional)

src/main/resources/
└── application.yaml

src/test/java/com/learnjava/library_events_producer_v2/
├── controller/
│   └── LibraryEventControllerTest.java
├── service/
│   └── LibraryEventServiceTest.java
└── producer/
    └── LibraryEventProducerTest.java
```

---

## Success Metrics

- ✓ Both POST and PUT endpoints functional
- ✓ Events published to Kafka successfully
- ✓ Proper validation and error handling
- ✓ All requests logged appropriately
- ✓ Unit tests pass with 80%+ coverage
- ✓ Application starts without errors
- ✓ Kafka connectivity verified
- ✓ Response times within 2 seconds

---

**Status:** Ready for Implementation  
**Next Step:** Start with Layer 1 - Data Models

