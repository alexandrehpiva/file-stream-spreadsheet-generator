package com.filestreamer.spreadsheetgenerator.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GcpPresignedUrlUtilTest {
    
    @Mock
    private Storage storage;
    
    @Mock
    private GoogleCredentials googleCredentials;
    
    @Mock
    private StorageOptions storageOptions;
    
    @Mock
    private StorageOptions.Builder storageOptionsBuilder;
    
    private GcpPresignedUrlUtil gcpPresignedUrlUtil;
    
    @BeforeEach
    void setUp() {
        gcpPresignedUrlUtil = new GcpPresignedUrlUtil();
        
        // Configurar campos via reflection
        ReflectionTestUtils.setField(gcpPresignedUrlUtil, "projectId", "test-project");
        ReflectionTestUtils.setField(gcpPresignedUrlUtil, "bucketName", "test-bucket");
    }
    
    @Test
    void shouldReturnTrueWhenConfiguredCorrectly() {
        // When
        boolean isConfigured = gcpPresignedUrlUtil.isConfigured();
        
        // Then
        assertTrue(isConfigured);
    }
    
    @Test
    void shouldReturnFalseWhenProjectIdIsEmpty() {
        // Given
        ReflectionTestUtils.setField(gcpPresignedUrlUtil, "projectId", "");
        
        // When
        boolean isConfigured = gcpPresignedUrlUtil.isConfigured();
        
        // Then
        assertFalse(isConfigured);
    }
    
    @Test
    void shouldReturnFalseWhenBucketNameIsNull() {
        // Given
        ReflectionTestUtils.setField(gcpPresignedUrlUtil, "bucketName", null);
        
        // When
        boolean isConfigured = gcpPresignedUrlUtil.isConfigured();
        
        // Then
        assertFalse(isConfigured);
    }
    
    @Test
    void shouldGeneratePresignedUrlSuccessfully() throws Exception {
        // Given
        String objectName = "exports/products_20240101.csv";
        URL expectedUrl = new URL("https://storage.googleapis.com/test-bucket/exports/products_20240101.csv?X-Goog-Signature=test");
        
        ReflectionTestUtils.setField(gcpPresignedUrlUtil, "storage", storage);
        
        when(storage.signUrl(any(BlobInfo.class), eq(1L), eq(TimeUnit.HOURS), any(Storage.SignUrlOption.class)))
                .thenReturn(expectedUrl);
        
        // When
        String presignedUrl = gcpPresignedUrlUtil.generatePresignedDownloadUrl(objectName);
        
        // Then
        assertNotNull(presignedUrl);
        assertEquals(expectedUrl.toString(), presignedUrl);
        
        verify(storage).signUrl(any(BlobInfo.class), eq(1L), eq(TimeUnit.HOURS), any(Storage.SignUrlOption.class));
    }
    
    @Test
    void shouldThrowExceptionWhenNotConfigured() {
        // Given
        ReflectionTestUtils.setField(gcpPresignedUrlUtil, "projectId", "");
        String objectName = "test-file.csv";
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> gcpPresignedUrlUtil.generatePresignedDownloadUrl(objectName));
        
        assertEquals("Credenciais GCP não configuradas", exception.getMessage());
    }
    
    @Test
    void shouldHandleExceptionDuringUrlGeneration() {
        // Given
        String objectName = "test-file.csv";
        ReflectionTestUtils.setField(gcpPresignedUrlUtil, "storage", storage);
        
        when(storage.signUrl(any(BlobInfo.class), anyLong(), any(TimeUnit.class), any(Storage.SignUrlOption.class)))
            .thenThrow(new RuntimeException("GCP Error"));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> gcpPresignedUrlUtil.generatePresignedDownloadUrl(objectName));
        
        assertEquals("Erro ao gerar URL pré-assinada do GCS", exception.getMessage());
    }
    
    @Test
    void shouldCreateStorageClientWhenNotInitialized() throws IOException {
        // Given
        String objectName = "test-file.csv";
        URL expectedUrl = new URL("https://storage.googleapis.com/test-bucket/test-file.csv?X-Goog-Signature=test");
        
        try (MockedStatic<GoogleCredentials> credentialsMock = mockStatic(GoogleCredentials.class);
             MockedStatic<StorageOptions> storageOptionsMock = mockStatic(StorageOptions.class)) {
            
            credentialsMock.when(GoogleCredentials::getApplicationDefault).thenReturn(googleCredentials);
            storageOptionsMock.when(StorageOptions::newBuilder).thenReturn(storageOptionsBuilder);
            
            when(storageOptionsBuilder.setProjectId("test-project")).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.setCredentials(googleCredentials)).thenReturn(storageOptionsBuilder);
            when(storageOptionsBuilder.build()).thenReturn(storageOptions);
            when(storageOptions.getService()).thenReturn(storage);
            
            when(storage.signUrl(any(BlobInfo.class), eq(1L), eq(TimeUnit.HOURS), any(Storage.SignUrlOption.class)))
                    .thenReturn(expectedUrl);
            
            // When
            String presignedUrl = gcpPresignedUrlUtil.generatePresignedDownloadUrl(objectName);
            
            // Then
            assertNotNull(presignedUrl);
            assertEquals(expectedUrl.toString(), presignedUrl);
            
            credentialsMock.verify(GoogleCredentials::getApplicationDefault);
            verify(storageOptionsBuilder).setProjectId("test-project");
            verify(storageOptionsBuilder).setCredentials(googleCredentials);
        }
    }
} 