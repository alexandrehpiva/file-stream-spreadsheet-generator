package com.filestreamer.spreadsheetgenerator.service.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsageExampleTest {

    @Mock
    private GenericLocalStreamExporter localExporter;

    @Mock
    private GenericS3StreamExporter s3Exporter;

    @Mock
    private GenericGcpStreamExporter gcpExporter;

    @Mock
    private FileNameGenerator fileNameGenerator;

    @InjectMocks
    private UsageExample usageExample;

    private ExportResult mockResult;

    @BeforeEach
    void setUp() {
        mockResult = new ExportResult("test_file.csv", "/path/to/file", "http://url", 3L, 1024L, 100L, ExporterType.LOCAL);
    }

    @Test
    void shouldExportSalesDataSuccessfully() throws IOException {
        // Given
        String expectedFileName = "vendas_relatorio_202501010930.csv";
        when(fileNameGenerator.generateCsvFileName("vendas_relatorio"))
                .thenReturn(expectedFileName);
        when(localExporter.exportData(any(Stream.class), any(ExportConfig.class)))
                .thenReturn(mockResult);

        // When
        ExportResult result = usageExample.exportSalesData();

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("test_file.csv", result.getFileName());
        assertEquals(3L, result.getTotalRecords());

        verify(fileNameGenerator).generateCsvFileName("vendas_relatorio");
        verify(localExporter).exportData(any(Stream.class), any(ExportConfig.class));
        verifyNoInteractions(s3Exporter, gcpExporter);
    }

    @Test
    void shouldExportUsersReportSuccessfully() throws IOException {
        // Given
        String expectedFileName = "usuarios_relatorio_202501010930.csv";
        when(fileNameGenerator.generateCsvFileName("usuarios_relatorio"))
                .thenReturn(expectedFileName);
        when(gcpExporter.exportData(any(Stream.class), any(ExportConfig.class)))
                .thenReturn(mockResult);

        // When
        ExportResult result = usageExample.exportUsersReport();

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("test_file.csv", result.getFileName());
        assertEquals(3L, result.getTotalRecords());

        verify(fileNameGenerator).generateCsvFileName("usuarios_relatorio");
        verify(gcpExporter).exportData(any(Stream.class), any(ExportConfig.class));
        verifyNoInteractions(localExporter, s3Exporter);
    }

    @Test
    void shouldExportFinancialDataSuccessfully() throws IOException {
        // Given
        String expectedFileName = "financeiro_mensal_202501010930.csv";
        when(fileNameGenerator.generateCsvFileName("financeiro_mensal"))
                .thenReturn(expectedFileName);
        when(s3Exporter.exportData(any(Stream.class), any(ExportConfig.class)))
                .thenReturn(mockResult);

        // When
        ExportResult result = usageExample.exportFinancialData();

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("test_file.csv", result.getFileName());
        assertEquals(3L, result.getTotalRecords());

        verify(fileNameGenerator).generateCsvFileName("financeiro_mensal");
        verify(s3Exporter).exportData(any(Stream.class), any(ExportConfig.class));
        verifyNoInteractions(localExporter, gcpExporter);
    }

    @Test
    void shouldExportGenericDataWithLocalExporter() throws IOException {
        // Given
        Stream<String[]> testData = Stream.of(
                new String[]{"valor1", "valor2"},
                new String[]{"valor3", "valor4"}
        );
        String[] headers = {"Coluna1", "Coluna2"};
        String expectedFileName = "dados_teste_202501010930.csv";
        
        when(fileNameGenerator.generateCsvFileName("dados_teste"))
                .thenReturn(expectedFileName);
        when(localExporter.exportData(any(Stream.class), any(ExportConfig.class)))
                .thenReturn(mockResult);

        // When
        ExportResult result = usageExample.exportGenericData(
                testData, headers, "dados_teste", "pasta/teste", ExporterType.LOCAL
        );

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        
        verify(fileNameGenerator).generateCsvFileName("dados_teste");
        verify(localExporter).exportData(any(), any(ExportConfig.class));
        verifyNoInteractions(s3Exporter, gcpExporter);
    }

    @Test
    void shouldExportGenericDataWithS3Exporter() throws IOException {
        // Given
        Stream<String[]> testData = Stream.<String[]>of(new String[]{"dados", "s3"});
        String[] headers = {"Header1", "Header2"};
        String expectedFileName = "s3_dados_202501010930.csv";
        
        when(fileNameGenerator.generateCsvFileName("s3_dados"))
                .thenReturn(expectedFileName);
        when(s3Exporter.exportData(any(Stream.class), any(ExportConfig.class)))
                .thenReturn(mockResult);

        // When
        ExportResult result = usageExample.exportGenericData(
                testData, headers, "s3_dados", "s3/bucket", ExporterType.AWS_S3
        );

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        
        verify(fileNameGenerator).generateCsvFileName("s3_dados");
        verify(s3Exporter).exportData(any(), any(ExportConfig.class));
        verifyNoInteractions(localExporter, gcpExporter);
    }

    @Test
    void shouldExportGenericDataWithGcpExporter() throws IOException {
        // Given
        Stream<String[]> testData = Stream.<String[]>of(new String[]{"dados", "gcp"});
        String[] headers = {"Header1", "Header2"};
        String expectedFileName = "gcp_dados_202501010930.csv";
        
        when(fileNameGenerator.generateCsvFileName("gcp_dados"))
                .thenReturn(expectedFileName);
        when(gcpExporter.exportData(any(Stream.class), any(ExportConfig.class)))
                .thenReturn(mockResult);

        // When
        ExportResult result = usageExample.exportGenericData(
                testData, headers, "gcp_dados", "gcp/storage", ExporterType.GCP_STORAGE
        );

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        
        verify(fileNameGenerator).generateCsvFileName("gcp_dados");
        verify(gcpExporter).exportData(any(), any(ExportConfig.class));
        verifyNoInteractions(localExporter, s3Exporter);
    }

    @Test
    void shouldThrowExceptionWhenSalesExportFails() throws IOException {
        // Given
        when(fileNameGenerator.generateCsvFileName("vendas_relatorio"))
                .thenReturn("vendas_file.csv");
        when(localExporter.exportData(any(Stream.class), any(ExportConfig.class)))
                .thenThrow(new IOException("Erro de I/O"));

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            usageExample.exportSalesData();
        });
        
        assertEquals("Erro de I/O", exception.getMessage());
        verify(localExporter).exportData(any(Stream.class), any(ExportConfig.class));
    }

    @Test
    void shouldThrowExceptionWhenUsersExportFails() throws IOException {
        // Given
        when(fileNameGenerator.generateCsvFileName("usuarios_relatorio"))
                .thenReturn("usuarios_file.csv");
        when(gcpExporter.exportData(any(Stream.class), any(ExportConfig.class)))
                .thenThrow(new IOException("Erro no GCP"));

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            usageExample.exportUsersReport();
        });
        
        assertEquals("Erro no GCP", exception.getMessage());
        verify(gcpExporter).exportData(any(Stream.class), any(ExportConfig.class));
    }

    @Test
    void shouldThrowExceptionWhenFinancialExportFails() throws IOException {
        // Given
        when(fileNameGenerator.generateCsvFileName("financeiro_mensal"))
                .thenReturn("financeiro_file.csv");
        when(s3Exporter.exportData(any(Stream.class), any(ExportConfig.class)))
                .thenThrow(new IOException("Erro no S3"));

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            usageExample.exportFinancialData();
        });
        
        assertEquals("Erro no S3", exception.getMessage());
        verify(s3Exporter).exportData(any(Stream.class), any(ExportConfig.class));
    }

    @Test
    void shouldThrowExceptionWhenGenericExportFails() throws IOException {
        // Given
        Stream<String[]> testData = Stream.<String[]>of(new String[]{"test"});
        String[] headers = {"Header"};
        
        when(fileNameGenerator.generateCsvFileName("dados_erro"))
                .thenReturn("dados_erro.csv");
        when(localExporter.exportData(any(Stream.class), any(ExportConfig.class)))
                .thenThrow(new IOException("Erro genérico"));

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            usageExample.exportGenericData(
                    testData, headers, "dados_erro", "pasta/erro", ExporterType.LOCAL
            );
        });
        
        assertEquals("Erro genérico", exception.getMessage());
    }

    @Test
    void shouldVerifyCorrectConfigurations() throws IOException {
        // Given
        when(fileNameGenerator.generateCsvFileName(anyString()))
                .thenReturn("test_file.csv");
        when(localExporter.exportData(any(Stream.class), any(ExportConfig.class)))
                .thenReturn(mockResult);

        // When
        usageExample.exportSalesData();

        // Then - Verifica se ExportConfig foi criado com configurações corretas
        verify(localExporter).exportData(any(Stream.class), argThat(config -> {
            assertNotNull(config);
            assertEquals("test_file.csv", config.getFileName());
            assertEquals("relatorios/vendas", config.getBasePath());
            assertEquals(500, config.getBatchSize());
            assertArrayEquals(new String[]{"Data", "Produto", "Preço Unit.", "Quantidade", "Total"}, 
                            config.getHeaders());
            return true;
        }));
    }
} 