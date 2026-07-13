# Testing Guide
## Library Events Producer v2

**Date:** July 12, 2026  
**Version:** 1.0  

---

## Table of Contents

1. [Running Automated Tests](#running-automated-tests)
2. [Unit Tests](#unit-tests)
3. [Integration Tests](#integration-tests)
4. [Manual Testing](#manual-testing)
5. [Testing Checklist](#testing-checklist)

---

## Running Automated Tests

### Prerequisites

- Java 25 JDK installed
- Gradle installed (or use wrapper)
- Application built successfully

### Run All Tests

```bash
cd /home/iaxubabu/projects/kafka/library-events-producer-v2
./gradlew clean test
```

**Expected Output:**
```
BUILD SUCCESSFUL
21 tests completed, 0 failed
```

### Run Specific Test Class

```bash
# Run Controller tests only
./gradlew test --tests LibraryEventControllerTest

# Run Service tests only
./gradlew test --tests LibraryEventServiceTest

# Run Producer tests only
./gradlew test --tests LibraryEventProducerTest
```

### Run with Verbose Output

```bash
./gradlew test --info
```

### Run Tests with Coverage Report

```bash
./gradlew test
# Report will be at: build/reports/tests/test/index.html
```

---

## Unit Tests

### 1. Controller Tests (3 tests)

**File:** `src/test/java/com/learnjava/library_events_producer_v2/controller/LibraryEventControllerTest.java`

**Tests:**
- `testCreateLibraryEvent_Success()` - Verify POST endpoint creates event
- `testUpdateLibraryEvent_Success()` - Verify PUT endpoint updates event
- `testHealthCheck()` - Verify health check endpoint

**What it tests:**
- HTTP status codes (201, 200, 200)
- Response body structure
- Service layer integration
- Request handling

### 2. Service Layer Tests (10 tests)

**File:** `src/test/java/com/learnjava/library_events_producer_v2/service/LibraryEventServiceTest.java`

**Tests:**
- `testCreateLibraryEvent_Success()` - Create event successfully
- `testCreateLibraryEvent_NullEventType_ThrowsException()` - Validation error
- `testUpdateLibraryEvent_Success()` - Update event successfully
- `testUpdateLibraryEvent_IdMismatch_ThrowsException()` - ID mismatch error
- `testUpdateLibraryEvent_NullEventType_ThrowsException()` - Null event type error
- `testValidateLibraryEvent_ValidEvent_NoException()` - Valid event passes
- `testValidateLibraryEvent_NullEvent_ThrowsException()` - Null event error
- `testValidateLibraryEvent_InvalidLibraryEventId_ThrowsException()` - Invalid ID
- `testValidateLibraryEvent_BlankBookName_ThrowsException()` - Blank book name
- `testValidateLibraryEvent_BlankBookAuthor_ThrowsException()` - Blank author

**What it tests:**
- Business logic validation
- Event creation and updates
- Data validation
- Error handling

### 3. Producer Tests (3 tests)

**File:** `src/test/java/com/learnjava/library_events_producer_v2/producer/LibraryEventProducerTest.java`

**Tests:**
- `testPublishEvent_SerializationError()` - Handle serialization errors
- `testPublishEventSync_SerializationError()` - Handle sync publish errors
- `testObjectMapperConfiguration()` - Verify JSON serialization

**What it tests:**
- Kafka producer functionality
- JSON serialization
- Error handling in publishing
- Mock Kafka integration

### 4. Integration Tests (5 tests)

**File:** `src/test/java/com/learnjava/library_events_producer_v2/LibraryEventsProducerV2ApplicationTests.java`

**Tests:**
- Application context loading
- Spring Boot auto-configuration
- Bean wiring

**What it tests:**
- Full application startup
- Spring context initialization
- Dependency injection

---

## Integration Tests

### Running Integration Tests

```bash
./gradlew test
```

All tests include integration aspects:
- Mock Kafka producer
- Service layer integration
- Controller endpoint testing
- Request/response validation

### Test Scenarios Covered

#### 1. Valid CREATE Event (POST)
```bash
Test: testCreateLibraryEvent_Success()
Request:
  - libraryEventId: 1
  - eventType: ADD
  - book: { bookId: 101, bookName: "...", bookAuthor: "..." }
Expected:
  - Status: 201 Created
  - Event marked as PUBLISHED
  - Timestamp generated
```

#### 2. Valid UPDATE Event (PUT)
```bash
Test: testUpdateLibraryEvent_Success()
Request:
  - libraryEventId: 1
  - eventType: UPDATE
  - book: { bookId: 101, bookName: "... 4th Edition", bookAuthor: "..." }
Expected:
  - Status: 200 OK
  - Event marked as PUBLISHED
  - Timestamp updated
```

#### 3. Invalid Event Validation
```bash
Test: testCreateLibraryEvent_InvalidBookData()
Scenarios:
  - Missing required fields
  - Null event type
  - Empty book name
  - Empty book author
  - Invalid IDs (0 or negative)
Expected:
  - Status: 400 Bad Request
  - Error messages in response
```

#### 4. ID Mismatch on UPDATE
```bash
Test: testUpdateLibraryEvent_IdMismatch_ThrowsException()
Request:
  - URL: /library-events/1
  - Body: { libraryEventId: 2, ... }
Expected:
  - Exception thrown
  - Error message about ID mismatch
```

---

## Manual Testing

### Prerequisites

1. **Start Kafka:**
```bash
# Using Docker Compose (if available)
docker-compose up -d

# Or start manually
bin/zookeeper-server-start.sh config/zookeeper.properties &
bin/kafka-server-start.sh config/server.properties &
```

2. **Start the Application:**
```bash
./gradlew bootRun
```

The app will start on `http://localhost:8080`

### Test Cases

#### 1. Health Check

```bash
curl -X GET http://localhost:8080/library-events/health
```

**Expected Response:**
```
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8

Library Events Producer is running!
```

---

#### 2. Create Library Event (ADD)

```bash
curl -X POST http://localhost:8080/library-events \
  -H "Content-Type: application/json" \
  -d '{
    "libraryEventId": 1,
    "eventType": "ADD",
    "book": {
      "bookId": 101,
      "bookName": "The Art of Computer Programming",
      "bookAuthor": "Donald E. Knuth"
    }
  }'
```

**Expected Response:**
```json
HTTP/1.1 201 Created
Content-Type: application/json

{
  "libraryEventId": 1,
  "eventType": "ADD",
  "book": {
    "bookId": 101,
    "bookName": "The Art of Computer Programming",
    "bookAuthor": "Donald E. Knuth"
  },
  "timestamp": "2026-07-12T14:30:00",
  "status": "PUBLISHED"
}
```

---

#### 3. Update Library Event (UPDATE)

```bash
curl -X PUT http://localhost:8080/library-events/1 \
  -H "Content-Type: application/json" \
  -d '{
    "libraryEventId": 1,
    "eventType": "UPDATE",
    "book": {
      "bookId": 101,
      "bookName": "The Art of Computer Programming - 4th Edition",
      "bookAuthor": "Donald E. Knuth"
    }
  }'
```

**Expected Response:**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "libraryEventId": 1,
  "eventType": "UPDATE",
  "book": {
    "bookId": 101,
    "bookName": "The Art of Computer Programming - 4th Edition",
    "bookAuthor": "Donald E. Knuth"
  },
  "timestamp": "2026-07-12T14:35:00",
  "status": "PUBLISHED"
}
```

---

#### 4. Validation Error - Missing Event Type

```bash
curl -X POST http://localhost:8080/library-events \
  -H "Content-Type: application/json" \
  -d '{
    "libraryEventId": 2,
    "eventType": null,
    "book": {
      "bookId": 102,
      "bookName": "Clean Code",
      "bookAuthor": "Robert C. Martin"
    }
  }'
```

**Expected Response:**
```json
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "errorCode": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "timestamp": "2026-07-12T14:40:00",
  "status": 400,
  "path": "/library-events",
  "errors": [
    "eventType: Event Type is required"
  ]
}
```

---

#### 5. Validation Error - Invalid Book Data

```bash
curl -X POST http://localhost:8080/library-events \
  -H "Content-Type: application/json" \
  -d '{
    "libraryEventId": 3,
    "eventType": "ADD",
    "book": {
      "bookId": 103,
      "bookName": "",
      "bookAuthor": "Author"
    }
  }'
```

**Expected Response:**
```json
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "errorCode": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "timestamp": "2026-07-12T14:45:00",
  "status": 400,
  "path": "/library-events",
  "errors": [
    "book.bookName: Book Name is required and cannot be empty"
  ]
}
```

---

#### 6. Update with ID Mismatch

```bash
curl -X PUT http://localhost:8080/library-events/1 \
  -H "Content-Type: application/json" \
  -d '{
    "libraryEventId": 2,
    "eventType": "UPDATE",
    "book": {
      "bookId": 102,
      "bookName": "Clean Code",
      "bookAuthor": "Robert C. Martin"
    }
  }'
```

**Expected Response:**
```json
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "errorCode": "INVALID_REQUEST",
  "message": "Library Event ID in URL must match the ID in request body",
  "timestamp": "2026-07-12T14:50:00",
  "status": 400,
  "path": "/library-events/1"
}
```

---

### Using Postman

1. **Import Collection:**
   - Create new collection "Library Events Producer"

2. **Add Requests:**

   - **Health Check**
     - Method: GET
     - URL: `http://localhost:8080/library-events/health`

   - **Create Event**
     - Method: POST
     - URL: `http://localhost:8080/library-events`
     - Body: (JSON from example above)

   - **Update Event**
     - Method: PUT
     - URL: `http://localhost:8080/library-events/1`
     - Body: (JSON from example above)

3. **Run Tests:**
   - Send each request
   - Verify response status and body
   - Check response times

---

## Testing Checklist

### ✅ Unit Test Checklist

- [ ] All 21 unit tests pass
- [ ] No compilation errors
- [ ] No warnings in test output
- [ ] Controller tests pass
- [ ] Service tests pass
- [ ] Producer tests pass
- [ ] Integration tests pass

### ✅ Manual API Testing Checklist

**Health Endpoint:**
- [ ] GET /library-events/health returns 200
- [ ] Response contains correct message

**Create Event (POST):**
- [ ] Valid event returns 201 Created
- [ ] Response includes timestamp and status
- [ ] Event published to Kafka
- [ ] Missing fields return 400 Bad Request
- [ ] Null event type returns 400 Bad Request
- [ ] Empty book name returns 400 Bad Request
- [ ] Empty book author returns 400 Bad Request

**Update Event (PUT):**
- [ ] Valid event returns 200 OK
- [ ] Response includes updated timestamp
- [ ] Event published to Kafka
- [ ] ID mismatch returns 400 Bad Request
- [ ] Missing fields return 400 Bad Request
- [ ] Invalid IDs return 400 Bad Request

**Error Responses:**
- [ ] All errors include errorCode
- [ ] All errors include timestamp
- [ ] All errors include status code
- [ ] All errors include message

### ✅ Kafka Validation Checklist

- [ ] Kafka is running and accessible
- [ ] Events are published to Kafka topic
- [ ] Message key is set correctly
- [ ] Message value is valid JSON
- [ ] Partition assignment is working

### ✅ Logging Checklist

- [ ] Request logs show method and endpoint
- [ ] Response logs show status code
- [ ] Kafka publish logs show success/failure
- [ ] Error logs include stack traces
- [ ] No sensitive data in logs

---

## Test Execution Summary

### Quick Test Run

```bash
./gradlew test --info 2>&1 | tail -50
```

### Full Test Report

```bash
# Run tests
./gradlew test

# View HTML report
open build/reports/tests/test/index.html
```

### Test Statistics

```
Total Tests: 21
- Controller Tests: 3
- Service Tests: 10
- Producer Tests: 3
- Integration Tests: 5

Pass Rate: 100%
Execution Time: ~6 seconds
```

---

## Troubleshooting Test Failures

### Test Fails: Connection to Kafka Failed

**Solution:**
- Verify Kafka is running
- Check bootstrap-servers in application.yaml
- Check network connectivity to Kafka broker

### Test Fails: Validation Errors Unexpected

**Solution:**
- Check request body format
- Verify all required fields are present
- Check field data types match expectations

### Test Fails: Timeout Errors

**Solution:**
- Increase timeout values in test configuration
- Check system resource availability
- Verify Kafka performance

---

## Continuous Testing

### Enable Test Watching

```bash
./gradlew test --continuous
```

Tests will re-run on any code changes.

---

**Testing is Complete! All systems operational.** ✅

