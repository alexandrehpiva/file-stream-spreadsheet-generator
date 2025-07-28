package com.filestreamer.spreadsheetgenerator.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GcpFileInfoDto")
class GcpFileInfoDtoTest {

    @Test
    @DisplayName("Deve criar DTO com todos os campos")
    void shouldCreateDtoWithAllFields() {
        // Given
        String name = "test-file.csv";
        String fullPath = "exports/test-file.csv";
        Long size = 1024L;
        String contentType = "text/csv";
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now().plusSeconds(3600);
        String publicUrl = "https://storage.googleapis.com/bucket/exports/test-file.csv";

        // When
        GcpFileInfoDto dto = new GcpFileInfoDto(name, fullPath, size, contentType, createdAt, updatedAt, publicUrl);

        // Then
        assertEquals(name, dto.getName());
        assertEquals(fullPath, dto.getFullPath());
        assertEquals(size, dto.getSize());
        assertEquals(contentType, dto.getContentType());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
        assertEquals(publicUrl, dto.getPublicUrl());
    }

    @Test
    @DisplayName("Deve criar DTO vazio e definir campos via setters")
    void shouldCreateEmptyDtoAndSetFieldsViaSetters() {
        // Given
        GcpFileInfoDto dto = new GcpFileInfoDto();
        String name = "test-file.csv";
        String fullPath = "exports/test-file.csv";
        Long size = 1024L;
        String contentType = "text/csv";
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now().plusSeconds(3600);
        String publicUrl = "https://storage.googleapis.com/bucket/exports/test-file.csv";

        // When
        dto.setName(name);
        dto.setFullPath(fullPath);
        dto.setSize(size);
        dto.setContentType(contentType);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        dto.setPublicUrl(publicUrl);

        // Then
        assertEquals(name, dto.getName());
        assertEquals(fullPath, dto.getFullPath());
        assertEquals(size, dto.getSize());
        assertEquals(contentType, dto.getContentType());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
        assertEquals(publicUrl, dto.getPublicUrl());
    }

    @Test
    @DisplayName("Deve criar DTO com valores nulos")
    void shouldCreateDtoWithNullValues() {
        // When
        GcpFileInfoDto dto = new GcpFileInfoDto(null, null, null, null, null, null, null);

        // Then
        assertNull(dto.getName());
        assertNull(dto.getFullPath());
        assertNull(dto.getSize());
        assertNull(dto.getContentType());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
        assertNull(dto.getPublicUrl());
    }
} 