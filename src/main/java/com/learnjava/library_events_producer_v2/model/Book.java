package com.learnjava.library_events_producer_v2.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Book entity representing book details in the library system.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book {

    @Positive(message = "Book ID must be a positive number")
    private Long bookId;

    @NotBlank(message = "Book Name is required and cannot be empty")
    private String bookName;

    @NotBlank(message = "Book Author is required and cannot be empty")
    private String bookAuthor;
}

