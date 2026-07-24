# PostgreSQL Implementation Plan
## Library Events Producer v2

**Date:** July 24, 2026  
**Version:** 1.0  
**Status:** In Progress  

---

## Overview

This document outlines the implementation plan for replacing the placeholder H2 configuration with a PostgreSQL-backed setup in `library-events-producer-v2`.

At the current stage, the project is successfully configured to boot with PostgreSQL datasource properties, but it does **not yet persist library events** because there are no JPA entities, repositories, or persistence workflows in the application layer.

---

## Current State Analysis

### What is already done

- Added `spring-boot-starter-data-jpa` to `build.gradle`
- Added PostgreSQL driver dependency `org.postgresql:postgresql`
- Removed the old H2-specific configuration from `src/main/resources/application.yaml`
- Added PostgreSQL datasource and JPA configuration
- Kept Kafka producer settings and server settings environment-variable friendly

### What is not implemented yet

- No `@Entity` classes exist yet
- No Spring Data repositories exist yet
- No schema migration tooling is configured yet
- No service logic writes events to PostgreSQL
- No tests validate database persistence behavior yet

---

## Objectives

The PostgreSQL implementation should achieve the following:

1. Replace H2 completely with PostgreSQL
2. Persist `LibraryEvent` records in the database
3. Persist `Book` details associated with each event
4. Keep Kafka publishing behavior intact
5. Make local and container-based development easy to run
6. Add tests for persistence and integration flow

---

## Target Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    REST API Layer                        │
│  (Controller - POST/PUT endpoints for /library-events)  │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              Service/Business Logic Layer               │
│  Validates input and coordinates DB + Kafka flow        │
└──────────────────────┬──────────────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
┌───────▼──────────────────┐   ┌──────▼───────────────────┐
│ PostgreSQL Persistence   │   │ Kafka Producer Layer     │
│ JPA entities/repository  │   │ Publishes event payloads │
└──────────────┬───────────┘   └──────────────────────────┘
               │
┌──────────────▼──────────────────────────────────────────┐
│                     PostgreSQL Database                  │
└─────────────────────────────────────────────────────────┘
```

---

## Layer-by-Layer Implementation Plan

### Layer 1: Configuration

**Files already updated:**
- `build.gradle`
- `src/main/resources/application.yaml`

**Completed changes:**
- Added JPA starter and PostgreSQL runtime driver
- Added datasource properties:
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
- Added Hibernate/PostgreSQL configuration

**Next improvements:**
- Add profile-specific config if needed, such as:
  - `application-local.yaml`
  - `application-docker.yaml`
  - `application-test.yaml`

---

### Layer 2: JPA Entity Modeling

**Files to update:**
- `src/main/java/com/learnjava/library_events_producer_v2/model/Book.java`
- `src/main/java/com/learnjava/library_events_producer_v2/model/LibraryEvent.java`

**Planned changes:**
- Convert `LibraryEvent` into a JPA entity
- Convert `Book` into a JPA entity or embedded/related entity depending on desired normalization
- Define table names explicitly
- Map `eventType` as a string enum
- Map `timestamp` as a PostgreSQL-compatible datetime column
- Decide relationship strategy:
  - `@OneToOne` if each event contains a single book snapshot
  - `@ManyToOne` if multiple events may reference the same book

**Recommended approach for this project:**
- Use `LibraryEvent` as the aggregate root
- Use `@ManyToOne` from `LibraryEvent` to `Book`
- Keep `bookId` as the primary key in `Book`
- Keep `libraryEventId` as the primary key in `LibraryEvent`

---

### Layer 3: Repository Layer

**Files to create:**
- `src/main/java/com/learnjava/library_events_producer_v2/repository/BookRepository.java`
- `src/main/java/com/learnjava/library_events_producer_v2/repository/LibraryEventRepository.java`

**Planned changes:**
- Create Spring Data JPA repositories
- Support create and update flows
- Add helper query methods if needed, such as:
  - `existsByLibraryEventId(...)`
  - `findByLibraryEventId(...)`

---

### Layer 4: Persistence Service Layer

**Files to create/update:**
- `src/main/java/com/learnjava/library_events_producer_v2/service/LibraryEventPersistenceService.java`
- `src/main/java/com/learnjava/library_events_producer_v2/service/LibraryEventService.java`

**Planned changes:**
- Add a dedicated persistence service for database operations
- Save or update `Book` before saving `LibraryEvent`
- Ensure timestamps and status are managed consistently
- Define transaction boundaries with `@Transactional`

**Recommended flow:**

1. Validate request payload
2. Initialize timestamp if missing
3. Persist `Book`
4. Persist `LibraryEvent`
5. Publish event to Kafka
6. Update event status if needed

**Design note:**
- If Kafka publishing fails after DB save, the row can remain stored with a failure status such as `FAILED`
- This provides traceability and retry potential

---

### Layer 5: Database Schema Management

**Recommended addition:**
- Add Flyway for schema migrations

**Suggested files to create:**
- `src/main/resources/db/migration/V1__create_books_and_library_events.sql`

**Why this matters:**
- Keeps schema creation versioned and predictable
- Avoids relying only on Hibernate `ddl-auto`
- Makes deployment safer across environments

**Suggested initial tables:**
- `books`
- `library_events`

**Suggested columns:**

`books`
- `book_id`
- `book_name`
- `book_author`

`library_events`
- `library_event_id`
- `event_type`
- `book_id`
- `timestamp`
- `status`

---

### Layer 6: API and Business Logic Impact

**Files to review:**
- `src/main/java/com/learnjava/library_events_producer_v2/controller/LibraryEventController.java`
- `src/main/java/com/learnjava/library_events_producer_v2/service/LibraryEventService.java`

**Expected behavior after persistence is added:**
- `POST /library-events` should save an event and publish it to Kafka
- `PUT /library-events/{libraryEventId}` should update an existing DB record and publish the update
- Validation rules should remain unchanged
- Error handling should cover:
  - duplicate IDs
  - missing records for update
  - PostgreSQL connectivity issues
  - transaction failures

---

### Layer 7: Testing Strategy

**Files to create/update:**
- `src/test/java/.../service/LibraryEventServiceTest.java`
- `src/test/java/.../LibraryEventsProducerV2ApplicationTests.java`
- `src/test/resources/application-test.yaml`

**Planned testing coverage:**
- Unit tests for repository-backed service logic
- Integration tests for PostgreSQL configuration
- API tests covering save + update behavior
- Failure-path tests for database unavailable conditions

**Recommended testing options:**
- Use Testcontainers PostgreSQL for integration tests
- Keep pure unit tests mocked and fast

---

## Environment Variable Plan

Use these variables for PostgreSQL environments:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/library_events
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SERVER_PORT=8080
KAFKA_TOPIC_NAME=library-events
KAFKA_PRODUCER_PUBLISH_TIMEOUT_SECONDS=5
```

---

## Local Development Plan

### Run PostgreSQL locally with Docker

```bash
docker run --name library-events-postgres \
  -e POSTGRES_DB=library_events \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

### Start the application

```bash
./gradlew bootRun
```

---

## Risks and Considerations

1. **No schema migration tool yet**
   - Risk: inconsistent schema across environments

2. **No persistence model yet**
   - Risk: PostgreSQL config exists, but no business data is stored

3. **Kafka + database consistency**
   - Risk: DB save may succeed while Kafka publish fails
   - Mitigation: keep status tracking and consider retry/outbox later

4. **Test reliability**
   - Risk: database-backed tests may become flaky without isolated test containers

---

## Recommended Implementation Sequence

- [x] Replace H2 config with PostgreSQL config
- [x] Add JPA and PostgreSQL dependencies
- [ ] Convert domain models to JPA entities
- [ ] Add repository interfaces
- [ ] Add persistence service and transactions
- [ ] Persist events during create/update operations
- [ ] Add migration scripts
- [ ] Add integration tests with PostgreSQL
- [ ] Update Docker/Compose setup to include PostgreSQL service
- [ ] Update documentation and runbook

---

## Definition of Done

The PostgreSQL implementation is complete when:

- The application starts successfully against PostgreSQL
- Library events are saved in PostgreSQL
- Book data is saved and associated correctly
- Update requests modify existing database records
- Kafka publishing still works as before
- Automated tests cover persistence behavior
- Documentation includes setup, environment variables, and troubleshooting

---

## Related Documents

- `docs/IMPLEMENTATION_PLAN.md`
- `docs/DOCKER_IMPLEMENTATION.md`
- `docs/README.md`
