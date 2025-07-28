package com.filestreamer.spreadsheetgenerator.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PresignedUrlResponseDto")
class PresignedUrlResponseDtoTest {

    @Test
    @DisplayName("Deve criar DTO com todos os campos")
    void shouldCreateDtoWithAllFields() {
        // Given
        String filePath = "exports/test-file.csv";
        String presignedUrl = "https://storage.googleapis.com/bucket/exports/test-file.csv?signature=abc123";
        Instant expiresAt = Instant.now().plusSeconds(3600);
        Long durationHours = 1L;
        Boolean success = true;
        String errorMessage = null;

        // When
        PresignedUrlResponseDto dto = new PresignedUrlResponseDto(filePath, presignedUrl, expiresAt, durationHours, success, errorMessage);

        // Then
        assertEquals(filePath, dto.getFilePath());
        assertEquals(presignedUrl, dto.getPresignedUrl());
        assertEquals(expiresAt, dto.getExpiresAt());
        assertEquals(durationHours, dto.getDurationHours());
        assertEquals(success, dto.getSuccess());
        assertEquals(errorMessage, dto.getErrorMessage());
    }

    @Test
    @DisplayName("Deve criar DTO com erro")
    void shouldCreateDtoWithError() {
        // Given
        String filePath = "exports/test-file.csv";
        String presignedUrl = null;
        Instant expiresAt = null;
        Long durationHours = null;
        Boolean success = false;
        String errorMessage = "Arquivo não encontrado";

        // When
        PresignedUrlResponseDto dto = new PresignedUrlResponseDto(filePath, presignedUrl, expiresAt, durationHours, success, errorMessage);

        // Then
        assertEquals(filePath, dto.getFilePath());
        assertNull(dto.getPresignedUrl());
        assertNull(dto.getExpiresAt());
        assertNull(dto.getDurationHours());
        assertEquals(success, dto.getSuccess());
        assertEquals(errorMessage, dto.getErrorMessage());
    }

    @Test
    @DisplayName("Deve criar DTO vazio e definir campos via setters")
    void shouldCreateEmptyDtoAndSetFieldsViaSetters() {
        // Given
        PresignedUrlResponseDto dto = new PresignedUrlResponseDto();
        String filePath = "exports/test-file.csv";
        String presignedUrl = "https://storage.googleapis.com/bucket/exports/test-file.csv?signature=abc123";
        Instant expiresAt = Instant.now().plusSeconds(3600);
        Long durationHours = 1L;
        Boolean success = true;
        String errorMessage = null;

        // When
        dto.setFilePath(filePath);
        dto.setPresignedUrl(presignedUrl);
        dto.setExpiresAt(expiresAt);
        dto.setDurationHours(durationHours);
        dto.setSuccess(success);
        dto.setErrorMessage(errorMessage);

        // Then
        assertEquals(filePath, dto.getFilePath());
        assertEquals(presignedUrl, dto.getPresignedUrl());
        assertEquals(expiresAt, dto.getExpiresAt());
        assertEquals(durationHours, dto.getDurationHours());
        assertEquals(success, dto.getSuccess());
        assertEquals(errorMessage, dto.getErrorMessage());
    }

    @Test
    @DisplayName("Deve criar DTO com valores nulos")
    void shouldCreateDtoWithNullValues() {
        // When
        PresignedUrlResponseDto dto = new PresignedUrlResponseDto(null, null, null, null, null, null);

        // Then
        assertNull(dto.getFilePath());
        assertNull(dto.getPresignedUrl());
        assertNull(dto.getExpiresAt());
        assertNull(dto.getDurationHours());
        assertNull(dto.getSuccess());
        assertNull(dto.getErrorMessage());
    }
} 