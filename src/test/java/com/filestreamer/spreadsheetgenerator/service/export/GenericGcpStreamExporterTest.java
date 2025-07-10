package com.filestreamer.spreadsheetgenerator.service.export;

import com.google.cloud.storage.*;
import com.google.cloud.WriteChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenericGcpStreamExporterTest {

    @InjectMocks
    private GenericGcpStreamExporter exporter;

    @Mock
    private Storage storage;
    
    @Mock
    private WriteChannel writeChannel;

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(exporter, "projectId", "test-project");
        ReflectionTestUtils.setField(exporter, "bucketName", "test-bucket");

        // We need to mock the static method getApplicationDefault
        try (MockedStatic<StorageOptions> storageOptionsMockedStatic = mockStatic(StorageOptions.class)) {
            StorageOptions.Builder builder = mock(StorageOptions.Builder.class);
            storageOptionsMockedStatic.when(StorageOptions::newBuilder).thenReturn(builder);
            when(builder.setProjectId(anyString())).thenReturn(builder);
            when(builder.setCredentials(any())).thenReturn(builder);
            when(builder.build()).thenReturn(mock(StorageOptions.class));
            when(builder.build().getService()).thenReturn(storage);
            
            // Inject the mocked storage client
            ReflectionTestUtils.setField(exporter, "storage", storage);
        }
        
        // Evita erro "no bytes written" no Java 24 ao escrever via Channels.newWriter
        when(writeChannel.write(any(java.nio.ByteBuffer.class))).thenAnswer(invocation -> {
            java.nio.ByteBuffer buffer = invocation.getArgument(0);
            int remaining = buffer.remaining();
            // Simula escrita completa avançando a posição do buffer
            buffer.position(buffer.limit());
            return remaining;
        });
    }

    @Test
    void shouldExportDataSuccessfully() throws IOException {
        // Given
        ExportConfig config = ExportConfig.builder()
                .fileName("test.csv")
                .headers(new String[]{"h1", "h2"})
                .build();
        Stream<String[]> data = Stream.of(new String[][]{{"d1", "d2"}});
        when(storage.writer(any(BlobInfo.class))).thenReturn(writeChannel);
        when(writeChannel.isOpen()).thenReturn(true);
        
        Blob blob = mock(Blob.class);
        when(storage.get(any(BlobId.class))).thenReturn(blob);
        when(blob.getSize()).thenReturn(123L);


        // When
        ExportResult result = exporter.exportData(data, config);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getTotalRecords());
        verify(storage).writer(any(BlobInfo.class));
    }

    @Test
    void shouldBuildObjectPathWithBasePath() throws IOException {
        // Given
        ExportConfig config = ExportConfig.builder()
                .fileName("test.csv")
                .basePath("data/reports/")
                .build();
        Stream<String[]> data = Stream.empty();
        ArgumentCaptor<BlobInfo> blobInfoCaptor = ArgumentCaptor.forClass(BlobInfo.class);
        when(storage.writer(any(BlobInfo.class))).thenReturn(writeChannel);
        when(writeChannel.isOpen()).thenReturn(true);

        // When
        exporter.exportData(data, config);

        // Then
        verify(storage).writer(blobInfoCaptor.capture());
        assertEquals("data/reports/test.csv", blobInfoCaptor.getValue().getBlobId().getName());
    }

    @Test
    void shouldHandleExceptionDuringUpload() throws IOException {
        // Given
        ExportConfig config = ExportConfig.builder().fileName("test.csv").build();
        Stream<String[]> data = Stream.of(new String[][]{{"d1", "d2"}});
        when(storage.writer(any(BlobInfo.class))).thenThrow(new RuntimeException("GCS Error"));

        // When
        ExportResult result = exporter.exportData(data, config);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("GCS Error"));
    }

    @Test
    void shouldReturnCorrectExporterInfo() {
        // When
        String info = exporter.getExporterInfo();

        // Then
        assertTrue(info.contains("Google Cloud Storage"));
        assertTrue(info.contains("test-project"));
    }

    @Test
    void shouldCheckConfigurationCorrectly() {
        // Given
        assertTrue(exporter.isConfigured());

        // When
        ReflectionTestUtils.setField(exporter, "projectId", "");
        
        // Then
        assertFalse(exporter.isConfigured());
    }

    @Test
    void shouldReturnErrorResultWhenNotConfigured() throws IOException {
        // Given
        ReflectionTestUtils.setField(exporter, "storage", null);
        ReflectionTestUtils.setField(exporter, "projectId", "");

        // When
        ExportResult result = exporter.exportData(Stream.empty(), ExportConfig.builder().fileName("f").build());

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Exportador não configurado"));
    }
} 