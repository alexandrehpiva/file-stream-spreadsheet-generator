package com.filestreamer.spreadsheetgenerator.service.export;

import com.opencsv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GenericLocalStreamExporterTest {

    @InjectMocks
    private GenericLocalStreamExporter exporter;

    @Mock
    private CSVWriter csvWriter;

    private final String TEST_PATH = "./temp-test";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exporter, "defaultExportPath", TEST_PATH);
        // Clean up test directory before each test
        try {
            Path path = Paths.get(TEST_PATH);
            if (Files.exists(path)) {
                Files.walk(path)
                     .sorted(java.util.Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(java.io.File::delete);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void shouldExportDataSuccessfully() throws IOException {
        // Given
        ExportConfig config = ExportConfig.builder()
                .fileName("test.csv")
                .headers(new String[]{"h1", "h2"})
                .basePath(TEST_PATH)
                .build();

        Stream<String[]> data = Stream.of(new String[][]{{"d1", "d2"}});

        // When
        ExportResult result = exporter.exportData(data, config);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getTotalRecords());
        assertTrue(Files.exists(Paths.get(TEST_PATH, "test.csv")));
    }
    
    @Test
    void shouldCreateDirectoryIfNotExists() throws IOException {
        // Given
        Path directory = Paths.get(TEST_PATH);
        assertFalse(Files.exists(directory));
        
        ExportConfig config = ExportConfig.builder().fileName("test.csv").basePath(TEST_PATH).build();
        Stream<String[]> data = Stream.empty();

        // When
        exporter.exportData(data, config);

        // Then
        assertTrue(Files.exists(directory));
    }
    
    @Test
    void shouldReturnErrorResultOnGeneralException() throws IOException {
        // Given
        ExportConfig config = ExportConfig.builder().fileName("test.csv").basePath("/invalid-path").build();
        Stream<String[]> data = Stream.of(new String[][]{{"d1", "d2"}});

        // When
        ExportResult result = exporter.exportData(data, config);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Erro durante exportação streaming"));
    }

    @Test
    void shouldUseCustomBasePathWhenProvided() throws IOException {
        // Given
        String customPath = TEST_PATH + "/custom";
        ExportConfig config = ExportConfig.builder().fileName("test.csv").basePath(customPath).build();
        Stream<String[]> data = Stream.empty();

        // When
        exporter.exportData(data, config);

        // Then
        assertTrue(Files.exists(Paths.get(customPath, "test.csv")));
    }

    @Test
    void shouldReturnExporterInfo() {
        // When
        String info = exporter.getExporterInfo();

        // Then
        assertTrue(info.contains("Local File System"));
        assertTrue(info.contains(TEST_PATH));
    }

    @Test
    void shouldReturnIsConfigured() {
        // When
        boolean configured = exporter.isConfigured();

        // Then
        assertTrue(configured);
    }
    
    @Test
    void shouldReturnNotConfiguredWhenPathIsNotWritable() {
        // Given
        // This is a bit tricky to test without actual permissions, so we simulate an exception
        ReflectionTestUtils.setField(exporter, "defaultExportPath", "/root/unwritable");

        // When
        boolean configured = exporter.isConfigured();
        
        // Then
        assertFalse(configured);
    }

    @Test
    void shouldReturnCorrectExporterType() {
        // When & Then
        assertEquals(ExporterType.LOCAL, exporter.getType());
    }
} 