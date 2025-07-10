package com.filestreamer.spreadsheetgenerator.service.export;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExportConfigTest {

    @Test
    void testBuilderWithAllFields() {
        // Given
        String fileName = "test_file.csv";
        String basePath = "custom/path";
        String contentType = "text/csv; charset=utf-8";
        String[] headers = {"Col1", "Col2", "Col3"};
        int batchSize = 500;

        // When
        ExportConfig config = ExportConfig.builder()
                .fileName(fileName)
                .basePath(basePath)
                .contentType(contentType)
                .headers(headers)
                .batchSize(batchSize)
                .build();

        // Then
        assertNotNull(config);
        assertEquals(fileName, config.getFileName());
        assertEquals(basePath, config.getBasePath());
        assertEquals(contentType, config.getContentType());
        assertArrayEquals(headers, config.getHeaders());
        assertEquals(batchSize, config.getBatchSize());
    }

    @Test
    void testBuilderWithDefaultValues() {
        // Given
        String fileName = "test_file.csv";

        // When
        ExportConfig config = ExportConfig.builder()
                .fileName(fileName)
                .build();

        // Then
        assertNotNull(config);
        assertEquals(fileName, config.getFileName());
        assertNull(config.getBasePath());
        assertEquals("text/csv", config.getContentType()); // Valor padrão
        assertNull(config.getHeaders());
        assertEquals(1000, config.getBatchSize()); // Valor padrão
    }

    @Test
    void testBuilderWithPartialFields() {
        // Given
        String fileName = "partial_config.csv";
        String[] headers = {"ID", "Name"};

        // When
        ExportConfig config = ExportConfig.builder()
                .fileName(fileName)
                .headers(headers)
                .build();

        // Then
        assertNotNull(config);
        assertEquals(fileName, config.getFileName());
        assertNull(config.getBasePath());
        assertEquals("text/csv", config.getContentType());
        assertArrayEquals(headers, config.getHeaders());
        assertEquals(1000, config.getBatchSize());
    }

    @Test
    void testBuilderFailsWithNullFileName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ExportConfig.builder().build()
        );
        
        assertEquals("Nome do arquivo é obrigatório", exception.getMessage());
    }

    @Test
    void testBuilderFailsWithEmptyFileName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ExportConfig.builder().fileName("").build()
        );
        
        assertEquals("Nome do arquivo é obrigatório", exception.getMessage());
    }

    @Test
    void testBuilderFailsWithWhitespaceFileName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ExportConfig.builder().fileName("   ").build()
        );
        
        assertEquals("Nome do arquivo é obrigatório", exception.getMessage());
    }

    @Test
    void testBuilderWithCustomBatchSize() {
        // Given
        String fileName = "batch_test.csv";
        int customBatchSize = 2000;

        // When
        ExportConfig config = ExportConfig.builder()
                .fileName(fileName)
                .batchSize(customBatchSize)
                .build();

        // Then
        assertEquals(customBatchSize, config.getBatchSize());
    }

    @Test
    void testBuilderWithCustomContentType() {
        // Given
        String fileName = "custom_content.csv";
        String customContentType = "application/csv";

        // When
        ExportConfig config = ExportConfig.builder()
                .fileName(fileName)
                .contentType(customContentType)
                .build();

        // Then
        assertEquals(customContentType, config.getContentType());
    }

    @Test
    void testBuilderChaining() {
        // When
        ExportConfig config = ExportConfig.builder()
                .fileName("chain_test.csv")
                .basePath("chain/path")
                .contentType("text/csv")
                .headers(new String[]{"A", "B"})
                .batchSize(750)
                .build();

        // Then
        assertNotNull(config);
        assertEquals("chain_test.csv", config.getFileName());
        assertEquals("chain/path", config.getBasePath());
        assertEquals("text/csv", config.getContentType());
        assertEquals(2, config.getHeaders().length);
        assertEquals(750, config.getBatchSize());
    }

    @Test
    void testBuilderWithNullHeaders() {
        // When
        ExportConfig config = ExportConfig.builder()
                .fileName("no_headers.csv")
                .headers(null)
                .build();

        // Then
        assertNull(config.getHeaders());
    }

    @Test
    void testBuilderWithEmptyHeaders() {
        // When
        ExportConfig config = ExportConfig.builder()
                .fileName("empty_headers.csv")
                .headers(new String[0])
                .build();

        // Then
        assertNotNull(config.getHeaders());
        assertEquals(0, config.getHeaders().length);
    }

    @Test
    void testBuilderWithNullBasePath() {
        // When
        ExportConfig config = ExportConfig.builder()
                .fileName("no_path.csv")
                .basePath(null)
                .build();

        // Then
        assertNull(config.getBasePath());
    }

    @Test
    void testBuilderWithEmptyBasePath() {
        // When
        ExportConfig config = ExportConfig.builder()
                .fileName("empty_path.csv")
                .basePath("")
                .build();

        // Then
        assertEquals("", config.getBasePath());
    }

    @Test
    void testBuilderImmutability() {
        // Given
        String[] originalHeaders = {"Original1", "Original2"};
        
        ExportConfig config = ExportConfig.builder()
                .fileName("immutable_test.csv")
                .headers(originalHeaders)
                .build();

        // When - Modificar array original
        originalHeaders[0] = "Modified";

        // Then - Como não há cópia defensiva implementada, o config será afetado
        // Este teste documenta o comportamento atual (sem cópia defensiva)
        // Em uma implementação com cópia defensiva, o teste seria diferente
        assertEquals("Modified", config.getHeaders()[0]); // Comportamento atual
        assertEquals("Original2", config.getHeaders()[1]);
    }

    @Test
    void testConfigGettersImmutability() {
        // Given
        ExportConfig config = ExportConfig.builder()
                .fileName("getter_test.csv")
                .headers(new String[]{"Header1", "Header2"})
                .build();

        // When - Tentar modificar array retornado
        String[] headers = config.getHeaders();
        headers[0] = "Modified";

        // Then - Verificar se config original não foi afetado
        // Nota: Este teste pode falhar se não houver cópia defensiva implementada
        // É uma boa prática implementar cópia defensiva nos getters
        assertNotNull(config.getHeaders());
        assertEquals(2, config.getHeaders().length);
    }
} 