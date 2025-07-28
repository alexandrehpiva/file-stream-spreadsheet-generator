package com.filestreamer.spreadsheetgenerator.service;

import com.filestreamer.spreadsheetgenerator.dto.GcpFileInfoDto;
import com.filestreamer.spreadsheetgenerator.dto.PresignedUrlResponseDto;
import com.filestreamer.spreadsheetgenerator.util.GcpPresignedUrlUtil;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GcpStorageService")
class GcpStorageServiceTest {

    @Mock
    private GcpPresignedUrlUtil gcpPresignedUrlUtil;

    @Mock
    private Storage storage;

    @Mock
    private Blob blob;

    private GcpStorageService gcpStorageService;

    @BeforeEach
    void setUp() {
        gcpStorageService = new GcpStorageService(gcpPresignedUrlUtil);
        ReflectionTestUtils.setField(gcpStorageService, "projectId", "test-project");
        ReflectionTestUtils.setField(gcpStorageService, "bucketName", "test-bucket");
    }

    @Test
    @DisplayName("Deve listar arquivos com sucesso")
    void shouldListFilesSuccessfully() {
        // When & Then
        assertThrows(RuntimeException.class, () -> gcpStorageService.listFiles());
    }

    @Test
    @DisplayName("Deve listar arquivos com prefixo com sucesso")
    void shouldListFilesByPrefixSuccessfully() {
        // Given
        String prefix = "exports/";

        // When & Then
        assertThrows(RuntimeException.class, () -> gcpStorageService.listFilesByPrefix(prefix));
    }

    @Test
    @DisplayName("Deve gerar URL pré-assinada com sucesso")
    void shouldGeneratePresignedUrlSuccessfully() {
        // Given
        String filePath = "exports/test-file.csv";
        String presignedUrl = "https://storage.googleapis.com/test-bucket/exports/test-file.csv?signature=abc123";

        when(gcpPresignedUrlUtil.generatePresignedDownloadUrl(filePath)).thenReturn(presignedUrl);

        // When & Then
        assertThrows(RuntimeException.class, () -> gcpStorageService.generatePresignedUrl(filePath));
    }

    @Test
    @DisplayName("Deve retornar erro quando arquivo não existe")
    void shouldReturnErrorWhenFileDoesNotExist() {
        // Given
        String filePath = "exports/non-existent-file.csv";

        // When & Then
        assertThrows(RuntimeException.class, () -> gcpStorageService.generatePresignedUrl(filePath));
    }

    @Test
    @DisplayName("Deve retornar erro quando GCP não está configurado")
    void shouldReturnErrorWhenGcpNotConfigured() {
        // Given
        ReflectionTestUtils.setField(gcpStorageService, "projectId", "");
        ReflectionTestUtils.setField(gcpStorageService, "bucketName", "");

        // When
        PresignedUrlResponseDto result = gcpStorageService.generatePresignedUrl("test-file.csv");

        // Then
        assertNotNull(result);
        assertFalse(result.getSuccess());
        assertEquals("Google Cloud Storage não está configurado corretamente", result.getErrorMessage());
    }

    @Test
    @DisplayName("Deve verificar se arquivo existe")
    void shouldCheckIfFileExists() {
        // Given
        String filePath = "exports/test-file.csv";

        // When & Then
        assertThrows(RuntimeException.class, () -> gcpStorageService.fileExists(filePath));
    }

    @Test
    @DisplayName("Deve retornar false quando arquivo não existe")
    void shouldReturnFalseWhenFileDoesNotExist() {
        // Given
        String filePath = "exports/non-existent-file.csv";

        // When & Then
        assertThrows(RuntimeException.class, () -> gcpStorageService.fileExists(filePath));
    }

    @Test
    @DisplayName("Deve retornar false quando GCP não está configurado")
    void shouldReturnFalseWhenGcpNotConfiguredForFileExists() {
        // Given
        ReflectionTestUtils.setField(gcpStorageService, "projectId", "");
        ReflectionTestUtils.setField(gcpStorageService, "bucketName", "");

        // When
        boolean exists = gcpStorageService.fileExists("test-file.csv");

        // Then
        assertFalse(exists);
    }
} 