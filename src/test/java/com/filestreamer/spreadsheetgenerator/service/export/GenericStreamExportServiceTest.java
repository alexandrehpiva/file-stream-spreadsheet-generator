package com.filestreamer.spreadsheetgenerator.service.export;

import com.filestreamer.spreadsheetgenerator.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenericStreamExportServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductDataFormatter productFormatter;

    @Mock
    private FileNameGenerator fileNameGenerator;

    @Mock
    private GenericLocalStreamExporter localExporter;

    @Mock
    private GenericS3StreamExporter s3Exporter;

    @Mock
    private GenericGcpStreamExporter gcpExporter;

    private GenericStreamExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new GenericStreamExportService(
                productRepository,
                productFormatter,
                fileNameGenerator,
                localExporter,
                s3Exporter,
                gcpExporter
        );
    }

    @Test
    void shouldExportAllProductsSuccessfully() throws IOException {
        // Given
        ExporterType exporterType = ExporterType.LOCAL;
        String basePath = "./test";
        String fileName = "products_export_20240624.csv";
        String[] headers = {"ID", "Name", "Description", "Price", "Created At", "Updated At"};

        ExportResult expectedResult = new ExportResult(fileName, "./test/" + fileName, 
                "file://./test/" + fileName, 2L, 1024L, 500L, ExporterType.LOCAL);

        // Mocks
        when(productRepository.findAllByOrderByCreatedAtStream()).thenReturn(Stream.empty());
        when(fileNameGenerator.generateCsvFileName("products_export")).thenReturn(fileName);
        when(productFormatter.getHeaders()).thenReturn(headers);
        when(localExporter.isConfigured()).thenReturn(true);
        when(localExporter.exportData(any(), any())).thenReturn(expectedResult);

        // When
        ExportResult result = exportService.exportAllProducts(exporterType, basePath);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(fileName, result.getFileName());
        
        // Verificar chamadas dos mocks
        verify(productRepository).findAllByOrderByCreatedAtStream();
        verify(fileNameGenerator).generateCsvFileName("products_export");
        verify(productFormatter).getHeaders();
        verify(localExporter).isConfigured();
        verify(localExporter).exportData(any(), any());
    }

    @Test
    void shouldExportFilteredProductsByPriceSuccessfully() throws IOException {
        // Given
        ExporterType exporterType = ExporterType.LOCAL;
        BigDecimal minPrice = BigDecimal.valueOf(150.00);
        String basePath = "./test";
        String fileName = "products_price_min_150.0_20240624.csv";
        String[] headers = {"ID", "Name", "Description", "Price", "Created At", "Updated At"};

        ExportResult expectedResult = new ExportResult(fileName, "./test/" + fileName, 
                "file://./test/" + fileName, 1L, 512L, 300L, ExporterType.LOCAL);

        // Mocks
        when(productRepository.findByPriceGreaterThanEqualStream(minPrice)).thenReturn(Stream.empty());
        when(fileNameGenerator.generateFilteredFileName("products", "price_min_" + minPrice, "csv")).thenReturn(fileName);
        when(productFormatter.getHeaders()).thenReturn(headers);
        when(localExporter.isConfigured()).thenReturn(true);
        when(localExporter.exportData(any(), any())).thenReturn(expectedResult);

        // When
        ExportResult result = exportService.exportProductsByMinPrice(exporterType, minPrice, basePath);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(fileName, result.getFileName());
        
        // Verificar chamadas dos mocks
        verify(productRepository).findByPriceGreaterThanEqualStream(minPrice);
        verify(fileNameGenerator).generateFilteredFileName("products", "price_min_" + minPrice, "csv");
        verify(productFormatter).getHeaders();
        verify(localExporter).isConfigured();
        verify(localExporter).exportData(any(), any());
    }

    @Test
    void shouldExportGenericDataSuccessfully() throws IOException {
        // Given
        ExporterType exporterType = ExporterType.AWS_S3;
        Stream<String[]> dataStream = Stream.of(
                new String[]{"1", "Test Data", "Description"},
                new String[]{"2", "More Test", "Another Description"}
        );
        
        ExportConfig exportConfig = ExportConfig.builder()
                .fileName("generic_data.csv")
                .basePath("s3://bucket/path")
                .headers(new String[]{"ID", "Name", "Description"})
                .build();

        ExportResult expectedResult = new ExportResult("generic_data.csv", "s3://bucket/path/generic_data.csv", 
                "https://s3.amazonaws.com/bucket/path/generic_data.csv", 2L, 256L, 400L, ExporterType.AWS_S3);

        // Mocks
        when(s3Exporter.isConfigured()).thenReturn(true);
        when(s3Exporter.exportData(dataStream, exportConfig)).thenReturn(expectedResult);

        // When
        ExportResult result = exportService.exportGenericData(dataStream, exporterType, exportConfig);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2L, result.getTotalRecords());
        assertEquals("generic_data.csv", result.getFileName());
        
        // Verificar chamadas dos mocks
        verify(s3Exporter).isConfigured();
        verify(s3Exporter).exportData(dataStream, exportConfig);
    }

    @Test
    void shouldThrowExceptionWhenExporterNotConfigured() {
        // Given
        ExporterType exporterType = ExporterType.GCP_STORAGE;
        String basePath = "./test";

        // Mocks - precisamos configurar getType() também
        when(gcpExporter.isConfigured()).thenReturn(false);
        when(gcpExporter.getType()).thenReturn(ExporterType.GCP_STORAGE);

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> exportService.exportAllProducts(exporterType, basePath)
        );

        assertTrue(exception.getMessage().contains("não está configurado corretamente"));
        verify(gcpExporter).isConfigured();
        verify(gcpExporter).getType();
    }

    @Test
    void shouldReturnExportersInformation() {
        // Given
        when(localExporter.getType()).thenReturn(ExporterType.LOCAL);
        when(localExporter.getExporterInfo()).thenReturn("Local File System");
        when(localExporter.isConfigured()).thenReturn(true);

        when(s3Exporter.getType()).thenReturn(ExporterType.AWS_S3);
        when(s3Exporter.getExporterInfo()).thenReturn("Amazon S3");
        when(s3Exporter.isConfigured()).thenReturn(false);

        when(gcpExporter.getType()).thenReturn(ExporterType.GCP_STORAGE);
        when(gcpExporter.getExporterInfo()).thenReturn("Google Cloud Storage");
        when(gcpExporter.isConfigured()).thenReturn(true);

        // When
        Map<ExporterType, ExporterInfo> result = exportService.getExportersInfo();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        assertTrue(result.containsKey(ExporterType.LOCAL));
        assertTrue(result.containsKey(ExporterType.AWS_S3));
        assertTrue(result.containsKey(ExporterType.GCP_STORAGE));

        assertEquals("Local File System", result.get(ExporterType.LOCAL).getInfo());
        assertTrue(result.get(ExporterType.LOCAL).isConfigured());
        
        assertEquals("Amazon S3", result.get(ExporterType.AWS_S3).getInfo());
        assertFalse(result.get(ExporterType.AWS_S3).isConfigured());
        
        assertEquals("Google Cloud Storage", result.get(ExporterType.GCP_STORAGE).getInfo());
        assertTrue(result.get(ExporterType.GCP_STORAGE).isConfigured());
    }

    @Test
    void shouldReturnAvailableExporters() {
        // Given
        when(localExporter.isConfigured()).thenReturn(true);
        when(localExporter.getType()).thenReturn(ExporterType.LOCAL);
        
        when(s3Exporter.isConfigured()).thenReturn(false);
        
        when(gcpExporter.isConfigured()).thenReturn(true);
        when(gcpExporter.getType()).thenReturn(ExporterType.GCP_STORAGE);

        // When
        List<ExporterType> result = exportService.getAvailableExporters();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(ExporterType.LOCAL));
        assertTrue(result.contains(ExporterType.GCP_STORAGE));
        assertFalse(result.contains(ExporterType.AWS_S3));
    }

    @Test
    void shouldUseCorrectExporterForEachType() throws IOException {
        // Given
        String basePath = "./test";
        ExportResult mockResult = new ExportResult("test.csv", "./test/test.csv", 
                "file://./test/test.csv", 0L, 0L, 0L, ExporterType.LOCAL);

        when(productRepository.findAllByOrderByCreatedAtStream()).thenReturn(Stream.empty());
        when(fileNameGenerator.generateCsvFileName(anyString())).thenReturn("test.csv");
        when(productFormatter.getHeaders()).thenReturn(new String[]{"ID"});

        // Test LOCAL
        when(localExporter.isConfigured()).thenReturn(true);
        when(localExporter.exportData(any(), any())).thenReturn(mockResult);
        exportService.exportAllProducts(ExporterType.LOCAL, basePath);
        verify(localExporter).exportData(any(), any());

        // Test S3
        when(s3Exporter.isConfigured()).thenReturn(true);
        when(s3Exporter.exportData(any(), any())).thenReturn(mockResult);
        exportService.exportAllProducts(ExporterType.AWS_S3, basePath);
        verify(s3Exporter).exportData(any(), any());

        // Test GCP
        when(gcpExporter.isConfigured()).thenReturn(true);
        when(gcpExporter.exportData(any(), any())).thenReturn(mockResult);
        exportService.exportAllProducts(ExporterType.GCP_STORAGE, basePath);
        verify(gcpExporter).exportData(any(), any());
    }

    @Test
    void shouldThrowExceptionForMinPriceExportWhenExporterNotConfigured() {
        // Given
        ExporterType exporterType = ExporterType.AWS_S3;
        BigDecimal minPrice = BigDecimal.TEN;
        String basePath = "s3://some-bucket";
        when(s3Exporter.isConfigured()).thenReturn(false);
        when(s3Exporter.getType()).thenReturn(ExporterType.AWS_S3);

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> exportService.exportProductsByMinPrice(exporterType, minPrice, basePath)
        );

        assertTrue(exception.getMessage().contains("Exportador Amazon S3 não está configurado corretamente"));
        verify(s3Exporter).isConfigured();
        verify(s3Exporter).getType();
    }

    @Test
    void shouldReturnOnlyConfiguredExporters() {
        // Given
        when(localExporter.isConfigured()).thenReturn(true);
        when(localExporter.getType()).thenReturn(ExporterType.LOCAL);
        when(s3Exporter.isConfigured()).thenReturn(false);
        when(gcpExporter.isConfigured()).thenReturn(true);
        when(gcpExporter.getType()).thenReturn(ExporterType.GCP_STORAGE);

        // When
        List<ExporterType> availableExporters = exportService.getAvailableExporters();

        // Then
        assertNotNull(availableExporters);
        assertEquals(2, availableExporters.size());
        assertTrue(availableExporters.contains(ExporterType.LOCAL));
        assertTrue(availableExporters.contains(ExporterType.GCP_STORAGE));
        assertFalse(availableExporters.contains(ExporterType.AWS_S3));
    }

    @Test
    void shouldReturnEmptyListWhenNoExportersAreConfigured() {
        // Given
        when(localExporter.isConfigured()).thenReturn(false);
        when(s3Exporter.isConfigured()).thenReturn(false);
        when(gcpExporter.isConfigured()).thenReturn(false);

        // When
        List<ExporterType> availableExporters = exportService.getAvailableExporters();

        // Then
        assertNotNull(availableExporters);
        assertTrue(availableExporters.isEmpty());
    }

    @Test
    void shouldHandleEmptyStreamForAllProductsExport() throws IOException {
        // Given
        ExporterType exporterType = ExporterType.LOCAL;
        String basePath = "./test";
        when(productRepository.findAllByOrderByCreatedAtStream()).thenReturn(Stream.empty());
        when(localExporter.isConfigured()).thenReturn(true);
        when(productFormatter.getHeaders()).thenReturn(new String[]{"header"});
        when(fileNameGenerator.generateCsvFileName(anyString())).thenReturn("empty_export.csv");
        
        ExportResult emptyResult = new ExportResult("empty_export.csv", "", "", 0L, 0L, 0L, ExporterType.LOCAL);
        when(localExporter.exportData(any(), any())).thenReturn(emptyResult);
        
        // When
        ExportResult result = exportService.exportAllProducts(exporterType, basePath);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalRecords());
        verify(productRepository).findAllByOrderByCreatedAtStream();
        verify(localExporter).exportData(any(), any());
    }
    
    @Test
    void shouldHandleEmptyStreamForFilteredProductsExport() throws IOException {
        // Given
        ExporterType exporterType = ExporterType.AWS_S3;
        BigDecimal minPrice = BigDecimal.valueOf(999);
        String basePath = "s3://test";
        when(productRepository.findByPriceGreaterThanEqualStream(minPrice)).thenReturn(Stream.empty());
        when(s3Exporter.isConfigured()).thenReturn(true);
        when(productFormatter.getHeaders()).thenReturn(new String[]{"header"});
        when(fileNameGenerator.generateFilteredFileName(anyString(), anyString(), anyString())).thenReturn("empty_filtered.csv");
        
        ExportResult emptyResult = new ExportResult("empty_filtered.csv", "", "", 0L, 0L, 0L, ExporterType.AWS_S3);
        when(s3Exporter.exportData(any(), any())).thenReturn(emptyResult);
        
        // When
        ExportResult result = exportService.exportProductsByMinPrice(exporterType, minPrice, basePath);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalRecords());
        verify(productRepository).findByPriceGreaterThanEqualStream(minPrice);
        verify(s3Exporter).exportData(any(), any());
    }
} 