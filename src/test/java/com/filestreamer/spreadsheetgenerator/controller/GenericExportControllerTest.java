package com.filestreamer.spreadsheetgenerator.controller;

import com.filestreamer.spreadsheetgenerator.service.export.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenericExportControllerTest {

    @Mock
    private GenericStreamExportService exportService;

    private GenericExportController controller;

    @BeforeEach
    void setUp() {
        controller = new GenericExportController(exportService);
    }

    @Test
    void shouldExportAllProductsSuccessfully() throws IOException {
        // Given
        ExporterType exporterType = ExporterType.LOCAL;
        String basePath = "./test";
        ExportResult successResult = new ExportResult("test.csv", "./test/test.csv", 
                "file://./test/test.csv", 100L, 2048L, 1500L, ExporterType.LOCAL);

        when(exportService.exportAllProducts(exporterType, basePath)).thenReturn(successResult);

        // When
        ResponseEntity<ExportResult> response = controller.exportAllProducts(exporterType, basePath);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(100L, response.getBody().getTotalRecords());
        assertEquals("test.csv", response.getBody().getFileName());

        verify(exportService).exportAllProducts(exporterType, basePath);
    }

    @Test
    void shouldReturnBadRequestWhenExportAllProductsFails() throws IOException {
        // Given
        ExporterType exporterType = ExporterType.LOCAL;
        String basePath = "./test";
        ExportResult failureResult = new ExportResult(ExporterType.LOCAL, "Erro na exportação");

        when(exportService.exportAllProducts(exporterType, basePath)).thenReturn(failureResult);

        // When
        ResponseEntity<ExportResult> response = controller.exportAllProducts(exporterType, basePath);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Erro na exportação", response.getBody().getErrorMessage());

        verify(exportService).exportAllProducts(exporterType, basePath);
    }

    @Test
    void shouldReturnBadRequestWhenExporterNotConfigured() throws IOException {
        // Given
        ExporterType exporterType = ExporterType.GCP_STORAGE;
        String basePath = "./test";

        when(exportService.exportAllProducts(exporterType, basePath))
                .thenThrow(new IllegalStateException("Exportador não configurado"));

        // When
        ResponseEntity<ExportResult> response = controller.exportAllProducts(exporterType, basePath);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getErrorMessage().contains("Exportador não configurado"));

        verify(exportService).exportAllProducts(exporterType, basePath);
    }

    @Test
    void shouldExportFilteredProductsSuccessfully() throws IOException {
        // Given
        ExporterType exporterType = ExporterType.AWS_S3;
        BigDecimal minPrice = BigDecimal.valueOf(100.00);
        String basePath = "s3://bucket/path";
        ExportResult successResult = new ExportResult("filtered.csv", "s3://bucket/path/filtered.csv", 
                "https://s3.amazonaws.com/bucket/path/filtered.csv", 25L, 1024L, 800L, ExporterType.AWS_S3);

        when(exportService.exportProductsByMinPrice(exporterType, minPrice, basePath)).thenReturn(successResult);

        // When
        ResponseEntity<ExportResult> response = controller.exportFilteredProducts(exporterType, minPrice, basePath);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(25L, response.getBody().getTotalRecords());
        assertEquals("filtered.csv", response.getBody().getFileName());

        verify(exportService).exportProductsByMinPrice(exporterType, minPrice, basePath);
    }

    @Test
    void shouldReturnBadRequestWhenFilteredExportFails() throws IOException {
        // Given
        ExporterType exporterType = ExporterType.AWS_S3;
        BigDecimal minPrice = BigDecimal.valueOf(100.00);
        String basePath = "s3://bucket/path";
        ExportResult failureResult = new ExportResult(ExporterType.AWS_S3, "Falha na exportação filtrada");

        when(exportService.exportProductsByMinPrice(exporterType, minPrice, basePath)).thenReturn(failureResult);

        // When
        ResponseEntity<ExportResult> response = controller.exportFilteredProducts(exporterType, minPrice, basePath);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Falha na exportação filtrada", response.getBody().getErrorMessage());

        verify(exportService).exportProductsByMinPrice(exporterType, minPrice, basePath);
    }

    @Test
    void shouldReturnInternalServerErrorForIOException() throws IOException {
        // Given
        ExporterType exporterType = ExporterType.LOCAL;
        String basePath = "./test";

        when(exportService.exportAllProducts(exporterType, basePath))
                .thenThrow(new IOException("Erro de I/O"));

        // When
        ResponseEntity<ExportResult> response = controller.exportAllProducts(exporterType, basePath);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getErrorMessage().contains("Erro de I/O"));

        verify(exportService).exportAllProducts(exporterType, basePath);
    }

    @Test
    void shouldReturnExportersInformation() {
        // Given
        Map<ExporterType, ExporterInfo> exportersInfo = Map.of(
                ExporterType.LOCAL, new ExporterInfo(ExporterType.LOCAL, "Local File System", true),
                ExporterType.AWS_S3, new ExporterInfo(ExporterType.AWS_S3, "Amazon S3", false),
                ExporterType.GCP_STORAGE, new ExporterInfo(ExporterType.GCP_STORAGE, "Google Cloud Storage", true)
        );

        when(exportService.getExportersInfo()).thenReturn(exportersInfo);

        // When
        ResponseEntity<Map<ExporterType, ExporterInfo>> response = controller.getExportersInfo();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertTrue(response.getBody().containsKey(ExporterType.LOCAL));
        assertTrue(response.getBody().containsKey(ExporterType.AWS_S3));
        assertTrue(response.getBody().containsKey(ExporterType.GCP_STORAGE));

        verify(exportService).getExportersInfo();
    }

    @Test
    void shouldReturnInternalServerErrorWhenFailsToGetExportersInformation() {
        // Given
        when(exportService.getExportersInfo()).thenThrow(new RuntimeException("Erro inesperado"));

        // When
        ResponseEntity<Map<ExporterType, ExporterInfo>> response = controller.getExportersInfo();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        verify(exportService).getExportersInfo();
    }

    @Test
    void shouldReturnAvailableExporters() {
        // Given
        List<ExporterType> availableExporters = Arrays.asList(ExporterType.LOCAL, ExporterType.GCP_STORAGE);

        when(exportService.getAvailableExporters()).thenReturn(availableExporters);

        // When
        ResponseEntity<List<ExporterType>> response = controller.getAvailableExporters();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains(ExporterType.LOCAL));
        assertTrue(response.getBody().contains(ExporterType.GCP_STORAGE));
        assertFalse(response.getBody().contains(ExporterType.AWS_S3));

        verify(exportService).getAvailableExporters();
    }

    @Test
    void shouldReturnInternalServerErrorWhenFailsToGetAvailableExporters() {
        // Given
        when(exportService.getAvailableExporters()).thenThrow(new RuntimeException("Erro inesperado"));

        // When
        ResponseEntity<List<ExporterType>> response = controller.getAvailableExporters();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        verify(exportService).getAvailableExporters();
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException() throws IOException {
        // Given
        ExporterType exporterType = ExporterType.LOCAL;
        String basePath = "./test";

        when(exportService.exportAllProducts(exporterType, basePath))
                .thenThrow(new RuntimeException("Erro inesperado"));

        // When
        ResponseEntity<ExportResult> response = controller.exportAllProducts(exporterType, basePath);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getErrorMessage().contains("Erro inesperado"));

        verify(exportService).exportAllProducts(exporterType, basePath);
    }
} 