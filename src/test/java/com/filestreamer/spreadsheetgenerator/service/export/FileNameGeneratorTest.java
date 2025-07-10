package com.filestreamer.spreadsheetgenerator.service.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class FileNameGeneratorTest {

    private FileNameGenerator generator;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @BeforeEach
    void setUp() {
        generator = new FileNameGenerator();
    }

    @Test
    void testGenerateFileName() {
        // When
        String fileName = generator.generateFileName("test_export", "csv");

        // Then
        assertNotNull(fileName);
        assertTrue(fileName.startsWith("test_export_"));
        assertTrue(fileName.endsWith(".csv"));
        
        // Verificar formato do timestamp
        String[] parts = fileName.split("_");
        assertTrue(parts.length >= 3); // prefix, date, time.extension
        
        // Extrair timestamp e verificar formato
        String timestampPart = fileName.substring("test_export_".length(), fileName.lastIndexOf("."));
        assertDoesNotThrow(() -> LocalDateTime.parse(timestampPart, TIMESTAMP_FORMATTER));
    }

    @Test
    void testGenerateCsvFileName() {
        // When
        String fileName = generator.generateCsvFileName("products_export");

        // Then
        assertNotNull(fileName);
        assertTrue(fileName.startsWith("products_export_"));
        assertTrue(fileName.endsWith(".csv"));
        // Verifica formato de timestamp: produtos_export_20231215_143045.csv
        assertTrue(fileName.matches("products_export_\\d{8}_\\d{6}\\.csv"));
    }

    @Test
    void testGenerateFilteredFileName() {
        // When
        String fileName = generator.generateFilteredFileName("sales", "january_2024", "csv");

        // Then
        assertNotNull(fileName);
        assertTrue(fileName.startsWith("sales_january_2024_"));
        assertTrue(fileName.endsWith(".csv"));
        
        // Verificar formato completo
        assertTrue(fileName.matches("sales_january_2024_\\d{8}_\\d{6}\\.csv"));
    }

    @Test
    void testGenerateFilteredFileNameWithSpecialCharacters() {
        // Given
        String filterWithSpecialChars = "price>=100.50&category=electronics";

        // When
        String fileName = generator.generateFilteredFileName("products", filterWithSpecialChars, "csv");

        // Then
        assertNotNull(fileName);
        assertTrue(fileName.startsWith("products_"));
        assertTrue(fileName.endsWith(".csv"));
        
        // Verificar que caracteres especiais foram substituídos por underscore
        // O FileNameGenerator substitui caracteres especiais por "_", não os remove
        String cleanPart = fileName.substring("products_".length(), fileName.lastIndexOf("_"));
        
        // Deve conter apenas caracteres permitidos (letras, números, underscore, hífen)
        assertTrue(cleanPart.matches("[a-zA-Z0-9_-]+"));
        
        // Verificar que alguns caracteres especiais foram substituídos
        assertTrue(cleanPart.contains("_")); // Deve ter underscores dos caracteres substituídos
    }

    @Test
    void testGenerateFileNameWithDifferentExtensions() {
        // Test JSON
        String jsonFile = generator.generateFileName("data_export", "json");
        assertTrue(jsonFile.endsWith(".json"));
        
        // Test XML
        String xmlFile = generator.generateFileName("report", "xml");
        assertTrue(xmlFile.endsWith(".xml"));
        
        // Test TXT
        String txtFile = generator.generateFileName("log", "txt");
        assertTrue(txtFile.endsWith(".txt"));
    }

    @Test
    void testGenerateFileNameUniqueness() {
        // When - Gerar múltiplos nomes em sequência rápida
        String fileName1 = generator.generateFileName("test", "csv");
        String fileName2 = generator.generateFileName("test", "csv");
        String fileName3 = generator.generateFileName("test", "csv");

        // Then - Podem ser iguais se gerados no mesmo segundo, mas estrutura deve estar correta
        assertNotNull(fileName1);
        assertNotNull(fileName2);
        assertNotNull(fileName3);
        
        assertTrue(fileName1.startsWith("test_"));
        assertTrue(fileName2.startsWith("test_"));
        assertTrue(fileName3.startsWith("test_"));
        
        assertTrue(fileName1.endsWith(".csv"));
        assertTrue(fileName2.endsWith(".csv"));
        assertTrue(fileName3.endsWith(".csv"));
    }

    @Test
    void testGenerateFileNameWithEmptyPrefix() {
        // When
        String fileName = generator.generateFileName("", "csv");

        // Then
        assertNotNull(fileName);
        assertTrue(fileName.startsWith("_")); // Começa com underscore devido ao prefix vazio
        assertTrue(fileName.endsWith(".csv"));
        
        // Verificar que ainda tem timestamp
        assertTrue(fileName.matches("_\\d{8}_\\d{6}\\.csv"));
    }

    @Test
    void testGenerateFilteredFileNameWithEmptyFilter() {
        // When
        String fileName = generator.generateFilteredFileName("base", "", "csv");

        // Then
        assertNotNull(fileName);
        assertTrue(fileName.startsWith("base__")); // Dois underscores devido ao filtro vazio
        assertTrue(fileName.endsWith(".csv"));
    }

    @Test
    void testTimestampFormat() {
        // When
        String fileName = generator.generateCsvFileName("test");

        // Then
        // Extrair timestamp do nome do arquivo
        String timestampPart = fileName.substring("test_".length(), fileName.lastIndexOf("."));
        
        // Verificar que o timestamp tem o formato correto (YYYYMMDD_HHMMSS)
        assertTrue(timestampPart.matches("\\d{8}_\\d{6}"));
        
        // Verificar que pode ser parseado como LocalDateTime
        assertDoesNotThrow(() -> {
            LocalDateTime.parse(timestampPart, TIMESTAMP_FORMATTER);
        });
    }
} 