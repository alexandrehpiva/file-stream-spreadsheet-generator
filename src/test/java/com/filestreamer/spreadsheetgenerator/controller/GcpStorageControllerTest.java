package com.filestreamer.spreadsheetgenerator.controller;

import com.filestreamer.spreadsheetgenerator.dto.GcpFileInfoDto;
import com.filestreamer.spreadsheetgenerator.dto.PresignedUrlResponseDto;
import com.filestreamer.spreadsheetgenerator.service.GcpStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GcpStorageController")
class GcpStorageControllerTest {

    @Mock
    private GcpStorageService gcpStorageService;

    private GcpStorageController gcpStorageController;

    @BeforeEach
    void setUp() {
        gcpStorageController = new GcpStorageController(gcpStorageService);
    }

    @Test
    @DisplayName("Deve listar todos os arquivos com sucesso")
    void shouldListAllFilesSuccessfully() {
        // Given
        List<GcpFileInfoDto> files = Arrays.asList(
                new GcpFileInfoDto("file1.csv", "exports/file1.csv", 1024L, "text/csv", 
                                  Instant.now(), Instant.now(), "https://storage.googleapis.com/bucket/exports/file1.csv"),
                new GcpFileInfoDto("file2.csv", "exports/file2.csv", 2048L, "text/csv", 
                                  Instant.now(), Instant.now(), "https://storage.googleapis.com/bucket/exports/file2.csv")
        );

        when(gcpStorageService.listFiles()).thenReturn(files);

        // When
        ResponseEntity<List<GcpFileInfoDto>> response = gcpStorageController.listAllFiles();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(gcpStorageService).listFiles();
    }

    @Test
    @DisplayName("Deve retornar erro quando GCP não está configurado")
    void shouldReturnErrorWhenGcpNotConfigured() {
        // Given
        when(gcpStorageService.listFiles()).thenThrow(new IllegalStateException("GCP não configurado"));

        // When
        ResponseEntity<List<GcpFileInfoDto>> response = gcpStorageController.listAllFiles();

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(gcpStorageService).listFiles();
    }

    @Test
    @DisplayName("Deve listar arquivos com prefixo com sucesso")
    void shouldListFilesByPrefixSuccessfully() {
        // Given
        String prefix = "exports/";
        List<GcpFileInfoDto> files = Arrays.asList(
                new GcpFileInfoDto("file1.csv", "exports/file1.csv", 1024L, "text/csv", 
                                  Instant.now(), Instant.now(), "https://storage.googleapis.com/bucket/exports/file1.csv")
        );

        when(gcpStorageService.listFilesByPrefix(prefix)).thenReturn(files);

        // When
        ResponseEntity<List<GcpFileInfoDto>> response = gcpStorageController.listFilesByPrefix(prefix);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(gcpStorageService).listFilesByPrefix(prefix);
    }

    @Test
    @DisplayName("Deve gerar URL pré-assinada com sucesso")
    void shouldGeneratePresignedUrlSuccessfully() {
        // Given
        String filePath = "exports/test-file.csv";
        String presignedUrl = "https://storage.googleapis.com/bucket/exports/test-file.csv?signature=abc123";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        PresignedUrlResponseDto expectedResponse = new PresignedUrlResponseDto(
                filePath, presignedUrl, expiresAt, 1L, true, null
        );

        when(gcpStorageService.generatePresignedUrl(filePath)).thenReturn(expectedResponse);

        // When
        ResponseEntity<PresignedUrlResponseDto> response = gcpStorageController.generatePresignedUrl(filePath);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(filePath, response.getBody().getFilePath());
        assertEquals(presignedUrl, response.getBody().getPresignedUrl());
        assertTrue(response.getBody().getSuccess());
        verify(gcpStorageService).generatePresignedUrl(filePath);
    }

    @Test
    @DisplayName("Deve retornar erro quando falha ao gerar URL pré-assinada")
    void shouldReturnErrorWhenPresignedUrlGenerationFails() {
        // Given
        String filePath = "exports/non-existent-file.csv";
        PresignedUrlResponseDto errorResponse = new PresignedUrlResponseDto(
                filePath, null, null, null, false, "Arquivo não encontrado no bucket"
        );

        when(gcpStorageService.generatePresignedUrl(filePath)).thenReturn(errorResponse);

        // When
        ResponseEntity<PresignedUrlResponseDto> response = gcpStorageController.generatePresignedUrl(filePath);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(filePath, response.getBody().getFilePath());
        assertFalse(response.getBody().getSuccess());
        assertEquals("Arquivo não encontrado no bucket", response.getBody().getErrorMessage());
        verify(gcpStorageService).generatePresignedUrl(filePath);
    }

    @Test
    @DisplayName("Deve verificar se arquivo existe com sucesso")
    void shouldCheckFileExistsSuccessfully() {
        // Given
        String filePath = "exports/test-file.csv";
        when(gcpStorageService.fileExists(filePath)).thenReturn(true);

        // When
        ResponseEntity<Boolean> response = gcpStorageController.fileExists(filePath);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(gcpStorageService).fileExists(filePath);
    }

    @Test
    @DisplayName("Deve retornar false quando arquivo não existe")
    void shouldReturnFalseWhenFileDoesNotExist() {
        // Given
        String filePath = "exports/non-existent-file.csv";
        when(gcpStorageService.fileExists(filePath)).thenReturn(false);

        // When
        ResponseEntity<Boolean> response = gcpStorageController.fileExists(filePath);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
        verify(gcpStorageService).fileExists(filePath);
    }
} 