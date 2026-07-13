package com.learnjava.library_events_producer_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ErrorResponse DTO for standardized error responses.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private int status;
    private String path;
    private List<String> errors;

    public static ErrorResponse of(String errorCode, String message, int status, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(status)
                .path(path)
                .build();
    }

    public static ErrorResponse of(String errorCode, String message, int status, String path, List<String> errors) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(status)
                .path(path)
                .errors(errors)
                .build();
    }
}

