package com.filestreamer.spreadsheetgenerator.service.export;

import com.filestreamer.spreadsheetgenerator.util.S3PresignedUrlUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenericS3StreamExporterTest {

    private GenericS3StreamExporter exporter;

    @Mock
    private S3Client s3Client;
    
    @Mock
    private S3PresignedUrlUtil s3PresignedUrlUtil;

    @BeforeEach
    void setUp() {
        exporter = new GenericS3StreamExporter(s3PresignedUrlUtil);
        
        ReflectionTestUtils.setField(exporter, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(exporter, "region", "us-east-1");
        ReflectionTestUtils.setField(exporter, "accessKeyId", "test-key");
        ReflectionTestUtils.setField(exporter, "secretAccessKey", "test-secret");
        // Inject the mocked client for most tests
        ReflectionTestUtils.setField(exporter, "s3Client", s3Client);
        
        // Configurar comportamento padrão do mock para evitar NullPointerException
        when(s3PresignedUrlUtil.isConfigured()).thenReturn(false);
    }

    @Test
    void shouldExportDataSuccessfully() throws IOException {
        // Given
        ExportConfig config = ExportConfig.builder()
                .fileName("test.csv")
                .headers(new String[]{"h1", "h2"})
                .build();
        Stream<String[]> data = Stream.of(new String[][]{{"d1", "d2"}});

        // When
        ExportResult result = exporter.exportData(data, config);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getTotalRecords());
        assertEquals("test.csv", result.getFileName());
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void shouldBuildS3KeyWithBasePath() throws IOException {
        // Given
        ExportConfig config = ExportConfig.builder()
                .fileName("test.csv")
                .basePath("data/reports/")
                .build();
        Stream<String[]> data = Stream.empty();
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

        // When
        exporter.exportData(data, config);

        // Then
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        assertEquals("data/reports/test.csv", requestCaptor.getValue().key());
    }

    @Test
    void shouldHandleExceptionDuringUpload() throws IOException {
        // Given
        ExportConfig config = ExportConfig.builder().fileName("test.csv").build();
        Stream<String[]> data = Stream.of(new String[][]{{"d1", "d2"}});
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("S3 Error"));

        // When
        ExportResult result = exporter.exportData(data, config);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("S3 Error"));
    }

    @Test
    void shouldReturnCorrectExporterInfo() {
        // When
        String info = exporter.getExporterInfo();

        // Then
        assertTrue(info.contains("Amazon S3"));
        assertTrue(info.contains("test-bucket"));
    }

    @Test
    void shouldCheckConfigurationCorrectly() {
        // Given
        assertTrue(exporter.isConfigured());

        // When
        ReflectionTestUtils.setField(exporter, "bucketName", "");
        
        // Then
        assertFalse(exporter.isConfigured());
    }
    
    @Test
    void shouldReturnErrorWhenNotConfigured() throws IOException {
        // Given
        ReflectionTestUtils.setField(exporter, "bucketName", "");
 
        // When
        ExportResult result = exporter.exportData(Stream.empty(), ExportConfig.builder().fileName("f").build());
 
        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Exportador não configurado"));
    }
} 