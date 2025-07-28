package com.filestreamer.spreadsheetgenerator.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
        ReflectionTestUtils.setField(gcpPresignedUrlUtil, "credentialsPath", "/path/to/credentials.json");
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
    void shouldReturnFalseWhenCredentialsPathIsEmpty() {
        // Given
        ReflectionTestUtils.setField(gcpPresignedUrlUtil, "credentialsPath", "");
        
        // When
        boolean isConfigured = gcpPresignedUrlUtil.isConfigured();
        
        // Then
        assertFalse(isConfigured);
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
    void shouldThrowExceptionWhenCredentialsFileNotFound() {
        // Given
        String objectName = "test-file.csv";
        
        // When & Then
        assertThrows(RuntimeException.class, 
            () -> gcpPresignedUrlUtil.generatePresignedDownloadUrl(objectName));
    }
} 