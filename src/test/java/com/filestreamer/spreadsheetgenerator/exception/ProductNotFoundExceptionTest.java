package com.filestreamer.spreadsheetgenerator.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        // Given
        String message = "Product not found with ID: 123";

        // When
        ProductNotFoundException exception = new ProductNotFoundException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "Product not found";
        RuntimeException cause = new RuntimeException("Database connection failed");

        // When
        ProductNotFoundException exception = new ProductNotFoundException(message, cause);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {
        // Given
        String message = "Test exception";

        // When
        ProductNotFoundException exception = new ProductNotFoundException(message);

        // Then
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void shouldHandleNullMessage() {
        // Given
        String message = null;

        // When
        ProductNotFoundException exception = new ProductNotFoundException(message);

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void shouldBeThrowable() {
        // Given
        String message = "Product not found";

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            throw new ProductNotFoundException(message);
        });
    }
} 