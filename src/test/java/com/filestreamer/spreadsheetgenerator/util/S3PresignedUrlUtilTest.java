package com.filestreamer.spreadsheetgenerator.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3PresignedUrlUtilTest {
    
    @Mock
    private S3Presigner s3Presigner;
    
    @Mock
    private PresignedGetObjectRequest presignedGetObjectRequest;
    
    private S3PresignedUrlUtil s3PresignedUrlUtil;
    
    @BeforeEach
    void setUp() throws Exception {
        s3PresignedUrlUtil = new S3PresignedUrlUtil();
        
        // Configurar campos via reflection
        ReflectionTestUtils.setField(s3PresignedUrlUtil, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(s3PresignedUrlUtil, "region", "us-east-1");
        ReflectionTestUtils.setField(s3PresignedUrlUtil, "accessKeyId", "test-access-key");
        ReflectionTestUtils.setField(s3PresignedUrlUtil, "secretAccessKey", "test-secret-key");
    }
    
    @Test
    void shouldReturnTrueWhenConfiguredCorrectly() {
        // When
        boolean isConfigured = s3PresignedUrlUtil.isConfigured();
        
        // Then
        assertTrue(isConfigured);
    }
    
    @Test
    void shouldReturnFalseWhenBucketNameIsEmpty() {
        // Given
        ReflectionTestUtils.setField(s3PresignedUrlUtil, "bucketName", "");
        
        // When
        boolean isConfigured = s3PresignedUrlUtil.isConfigured();
        
        // Then
        assertFalse(isConfigured);
    }
    
    @Test
    void shouldReturnFalseWhenAccessKeyIsNull() {
        // Given
        ReflectionTestUtils.setField(s3PresignedUrlUtil, "accessKeyId", null);
        
        // When
        boolean isConfigured = s3PresignedUrlUtil.isConfigured();
        
        // Then
        assertFalse(isConfigured);
    }
    
    @Test
    void shouldGeneratePresignedUrlSuccessfully() throws Exception {
        // Given
        String s3Key = "exports/products_20240101.csv";
        ReflectionTestUtils.setField(s3PresignedUrlUtil, "s3Presigner", s3Presigner);
        
        // Mock URL
        URL mockUrl = new URL("https://test-bucket.s3.us-east-1.amazonaws.com/test-file.csv?X-Amz-Algorithm=AWS4-HMAC-SHA256");
        when(presignedGetObjectRequest.url()).thenReturn(mockUrl);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedGetObjectRequest);
        
        // When
        String presignedUrl = s3PresignedUrlUtil.generatePresignedDownloadUrl(s3Key);
        
        // Then
        assertNotNull(presignedUrl);
        assertTrue(presignedUrl.contains("test-bucket.s3.us-east-1.amazonaws.com"));
        assertTrue(presignedUrl.contains("X-Amz-Algorithm=AWS4-HMAC-SHA256"));
        
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }
    
    @Test
    void shouldThrowExceptionWhenNotConfigured() {
        // Given
        ReflectionTestUtils.setField(s3PresignedUrlUtil, "bucketName", "");
        String s3Key = "test-file.csv";
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> s3PresignedUrlUtil.generatePresignedDownloadUrl(s3Key));
        
        assertEquals("Credenciais AWS S3 não configuradas", exception.getMessage());
    }
    
    @Test
    void shouldHandleExceptionDuringUrlGeneration() {
        // Given
        String s3Key = "test-file.csv";
        ReflectionTestUtils.setField(s3PresignedUrlUtil, "s3Presigner", s3Presigner);
        
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
            .thenThrow(new RuntimeException("AWS Error"));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> s3PresignedUrlUtil.generatePresignedDownloadUrl(s3Key));
        
        assertEquals("Erro ao gerar URL pré-assinada do S3", exception.getMessage());
    }
    
    @Test
    void shouldClosePresignerCorrectly() {
        // Given
        ReflectionTestUtils.setField(s3PresignedUrlUtil, "s3Presigner", s3Presigner);
        
        // When
        s3PresignedUrlUtil.close();
        
        // Then
        verify(s3Presigner).close();
        
        // Verificar que foi setado como null
        Object presigner = ReflectionTestUtils.getField(s3PresignedUrlUtil, "s3Presigner");
        assertNull(presigner);
    }
} 