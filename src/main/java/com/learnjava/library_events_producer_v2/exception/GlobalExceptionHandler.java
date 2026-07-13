package com.learnjava.library_events_producer_v2.exception;

import com.learnjava.library_events_producer_v2.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for REST API endpoints.
 * Centralized error handling and HTTP status mapping.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors (400 Bad Request).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.warn("Validation error: {}", ex.getMessage());

        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.add(error.getField() + ": " + error.getDefaultMessage())
        );

        ErrorResponse errorResponse = ErrorResponse.of(
                "VALIDATION_ERROR",
                "Request validation failed",
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", ""),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle custom LibraryEventException.
     */
    @ExceptionHandler(LibraryEventException.class)
    public ResponseEntity<ErrorResponse> handleLibraryEventException(
            LibraryEventException ex,
            WebRequest request) {

        log.warn("LibraryEventException: {}", ex.getMessage());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // Map specific error codes to HTTP statuses
        if (ex.getErrorCode().equals("LIBRARY_EVENT_NOT_FOUND")) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex.getErrorCode().equals("LIBRARY_EVENT_DUPLICATE")) {
            status = HttpStatus.CONFLICT;
        } else if (ex.getErrorCode().equals("INVALID_REQUEST")) {
            status = HttpStatus.BAD_REQUEST;
        }

        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getErrorCode(),
                ex.getMessage(),
                status.value(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Handle Kafka publishing exceptions.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex,
            WebRequest request) {

        log.error("Illegal state error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                "INVALID_STATE",
                "Invalid operation state: " + ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle all other exceptions (500 Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

