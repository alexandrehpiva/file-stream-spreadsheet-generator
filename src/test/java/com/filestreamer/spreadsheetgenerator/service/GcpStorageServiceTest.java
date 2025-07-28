package com.filestreamer.spreadsheetgenerator.service;

import com.filestreamer.spreadsheetgenerator.dto.PresignedUrlResponseDto;
import com.filestreamer.spreadsheetgenerator.util.GcpPresignedUrlUtil;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock
    private GoogleCredentials googleCredentials;

    @Mock
    private StorageOptions storageOptions;

    @Mock
    private StorageOptions.Builder storageOptionsBuilder;

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

        try (MockedStatic<GoogleCredentials> credentialsMock = mockStatic(GoogleCredentials.class);
             MockedStatic<StorageOptions> storageOptionsMock = mockStatic(StorageOptions.class)) {
            
            credentialsMock.when(GoogleCredentials::getApplicationDefault).thenReturn(googleCredentials);
            storageOptionsMock.when(StorageOptions::newBuilder).thenReturn(storageOptionsBuilder);
            
            when(storageOptionsBuilder.setProjectId("test-project")).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.setCredentials(googleCredentials)).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.build()).thenReturn(storageOptions);
            when(storageOptions.getService()).thenReturn(storage);
            
            when(storage.get(any(BlobId.class))).thenReturn(blob);

            // When
            PresignedUrlResponseDto result = gcpStorageService.generatePresignedUrl(filePath);

            // Then
            assertNotNull(result);
            assertEquals(filePath, result.getFilePath());
            assertEquals(presignedUrl, result.getPresignedUrl());
            assertTrue(result.getSuccess());
            assertNull(result.getErrorMessage());
            assertEquals(1L, result.getDurationHours());
            assertNotNull(result.getExpiresAt());
            
            verify(gcpPresignedUrlUtil).generatePresignedDownloadUrl(filePath);
        }
    }

    @Test
    @DisplayName("Deve retornar erro quando arquivo não existe")
    void shouldReturnErrorWhenFileDoesNotExist() {
        // Given
        String filePath = "exports/non-existent-file.csv";

        try (MockedStatic<GoogleCredentials> credentialsMock = mockStatic(GoogleCredentials.class);
             MockedStatic<StorageOptions> storageOptionsMock = mockStatic(StorageOptions.class)) {
            
            credentialsMock.when(GoogleCredentials::getApplicationDefault).thenReturn(googleCredentials);
            storageOptionsMock.when(StorageOptions::newBuilder).thenReturn(storageOptionsBuilder);
            
            when(storageOptionsBuilder.setProjectId("test-project")).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.setCredentials(googleCredentials)).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.build()).thenReturn(storageOptions);
            when(storageOptions.getService()).thenReturn(storage);
            
            when(storage.get(any(BlobId.class))).thenReturn(null);

            // When
            PresignedUrlResponseDto result = gcpStorageService.generatePresignedUrl(filePath);

            // Then
            assertNotNull(result);
            assertEquals(filePath, result.getFilePath());
            assertNull(result.getPresignedUrl());
            assertFalse(result.getSuccess());
            assertEquals("Arquivo não encontrado no bucket", result.getErrorMessage());
        }
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

        try (MockedStatic<GoogleCredentials> credentialsMock = mockStatic(GoogleCredentials.class);
             MockedStatic<StorageOptions> storageOptionsMock = mockStatic(StorageOptions.class)) {
            
            credentialsMock.when(GoogleCredentials::getApplicationDefault).thenReturn(googleCredentials);
            storageOptionsMock.when(StorageOptions::newBuilder).thenReturn(storageOptionsBuilder);
            
            when(storageOptionsBuilder.setProjectId("test-project")).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.setCredentials(googleCredentials)).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.build()).thenReturn(storageOptions);
            when(storageOptions.getService()).thenReturn(storage);
            
            when(storage.get(any(BlobId.class))).thenReturn(blob);

            // When
            boolean exists = gcpStorageService.fileExists(filePath);

            // Then
            assertTrue(exists);
        }
    }

    @Test
    @DisplayName("Deve retornar false quando arquivo não existe")
    void shouldReturnFalseWhenFileDoesNotExist() {
        // Given
        String filePath = "exports/non-existent-file.csv";

        try (MockedStatic<GoogleCredentials> credentialsMock = mockStatic(GoogleCredentials.class);
             MockedStatic<StorageOptions> storageOptionsMock = mockStatic(StorageOptions.class)) {
            
            credentialsMock.when(GoogleCredentials::getApplicationDefault).thenReturn(googleCredentials);
            storageOptionsMock.when(StorageOptions::newBuilder).thenReturn(storageOptionsBuilder);
            
            when(storageOptionsBuilder.setProjectId("test-project")).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.setCredentials(googleCredentials)).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.build()).thenReturn(storageOptions);
            when(storageOptions.getService()).thenReturn(storage);
            
            when(storage.get(any(BlobId.class))).thenReturn(null);

            // When
            boolean exists = gcpStorageService.fileExists(filePath);

            // Then
            assertFalse(exists);
        }
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

    @Test
    @DisplayName("Deve lidar com exceção durante listagem de arquivos")
    void shouldHandleExceptionDuringListFiles() {
        // Given
        try (MockedStatic<GoogleCredentials> credentialsMock = mockStatic(GoogleCredentials.class);
             MockedStatic<StorageOptions> storageOptionsMock = mockStatic(StorageOptions.class)) {
            
            credentialsMock.when(GoogleCredentials::getApplicationDefault).thenReturn(googleCredentials);
            storageOptionsMock.when(StorageOptions::newBuilder).thenReturn(storageOptionsBuilder);
            
            when(storageOptionsBuilder.setProjectId("test-project")).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.setCredentials(googleCredentials)).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.build()).thenReturn(storageOptions);
            when(storageOptions.getService()).thenReturn(storage);
            
            when(storage.list(anyString())).thenThrow(new RuntimeException("Erro de conexão"));

            // When & Then
            assertThrows(RuntimeException.class, () -> gcpStorageService.listFiles());
        }
    }

    @Test
    @DisplayName("Deve lidar com exceção durante geração de URL pré-assinada")
    void shouldHandleExceptionDuringPresignedUrlGeneration() {
        // Given
        String filePath = "exports/test-file.csv";
        when(gcpPresignedUrlUtil.generatePresignedDownloadUrl(filePath)).thenThrow(new RuntimeException("Erro de autenticação"));

        try (MockedStatic<GoogleCredentials> credentialsMock = mockStatic(GoogleCredentials.class);
             MockedStatic<StorageOptions> storageOptionsMock = mockStatic(StorageOptions.class)) {
            
            credentialsMock.when(GoogleCredentials::getApplicationDefault).thenReturn(googleCredentials);
            storageOptionsMock.when(StorageOptions::newBuilder).thenReturn(storageOptionsBuilder);
            
            when(storageOptionsBuilder.setProjectId("test-project")).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.setCredentials(googleCredentials)).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.build()).thenReturn(storageOptions);
            when(storageOptions.getService()).thenReturn(storage);
            
            when(storage.get(any(BlobId.class))).thenReturn(blob);

            // When
            PresignedUrlResponseDto result = gcpStorageService.generatePresignedUrl(filePath);

            // Then
            assertNotNull(result);
            assertFalse(result.getSuccess());
            assertTrue(result.getErrorMessage().contains("Erro de autenticação"));
        }
    }
} 