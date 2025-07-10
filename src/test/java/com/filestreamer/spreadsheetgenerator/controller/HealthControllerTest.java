package com.filestreamer.spreadsheetgenerator.controller;

import com.filestreamer.spreadsheetgenerator.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private DataSource dataSource;

    @MockBean
    private BuildProperties buildProperties;

    private Connection mockConnection;
    private DatabaseMetaData mockMetaData;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockMetaData = mock(DatabaseMetaData.class);
        
        when(dataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockConnection.isValid(anyInt())).thenReturn(true);
        
        when(mockMetaData.getDatabaseProductName()).thenReturn("H2");
        when(mockMetaData.getDriverName()).thenReturn("H2 JDBC Driver");
        when(mockMetaData.getURL()).thenReturn("jdbc:h2:mem:test");
    }

    @Test
    void shouldReturnCompleteHealthCheckSuccessfully() throws Exception {
        // Given
        when(productService.countAll()).thenReturn(100L);
        
        if (buildProperties != null) {
            when(buildProperties.getVersion()).thenReturn("1.0.0");
            when(buildProperties.getTime()).thenReturn(Instant.now());
        }

        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("Spreadsheet Generator"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.components").exists())
                .andExpect(jsonPath("$.components.database").exists())
                .andExpect(jsonPath("$.components.database.status").value("UP"))
                .andExpect(jsonPath("$.components.database.database").value("H2"))
                .andExpect(jsonPath("$.components.productService").exists())
                .andExpect(jsonPath("$.components.productService.status").value("UP"))
                .andExpect(jsonPath("$.components.productService.totalProducts").value(100))
                .andExpect(jsonPath("$.components.jvm").exists())
                .andExpect(jsonPath("$.components.jvm.status").value("UP"))
                .andExpect(jsonPath("$.components.jvm.javaVersion").exists())
                .andExpect(jsonPath("$.components.jvm.memory").exists());

        verify(productService).countAll();
        verify(dataSource).getConnection();
    }

    @Test
    void shouldReturnHealthCheckWithDownStatusWhenDatabaseFails() throws Exception {
        // Given
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));
        when(productService.countAll()).thenReturn(50L);

        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.components.database.status").value("DOWN"))
                .andExpect(jsonPath("$.components.database.error").value("Connection failed"))
                .andExpect(jsonPath("$.components.productService.status").value("UP"));
    }

    @Test
    void shouldReturnHealthCheckWithDownStatusWhenServiceFails() throws Exception {
        // Given
        when(productService.countAll()).thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.components.database.status").value("UP"))
                .andExpect(jsonPath("$.components.productService.status").value("DOWN"))
                .andExpect(jsonPath("$.components.productService.error").value("Service unavailable"));
    }

    @Test
    void shouldReturnHealthCheckWithInvalidDatabase() throws Exception {
        // Given
        when(mockConnection.isValid(anyInt())).thenReturn(false);
        when(productService.countAll()).thenReturn(25L);

        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.components.database.status").value("DOWN"));
    }

    @Test
    void shouldReturnSimpleHealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health/simple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnApplicationInfoWithBuildProperties() throws Exception {
        // Given
        when(productService.countAll()).thenReturn(150L);
        
        if (buildProperties != null) {
            when(buildProperties.getVersion()).thenReturn("2.0.0");
            when(buildProperties.getTime()).thenReturn(Instant.parse("2023-01-01T00:00:00Z"));
            when(buildProperties.getGroup()).thenReturn("com.filestreamer");
            when(buildProperties.getArtifact()).thenReturn("spreadsheet-generator");
        }

        // When & Then
        mockMvc.perform(get("/api/health/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("Spreadsheet Generator"))
                .andExpect(jsonPath("$.description").value("Sistema para processamento de planilhas com streaming CSV"))
                .andExpect(jsonPath("$.statistics").exists())
                .andExpect(jsonPath("$.statistics.totalProducts").value(150))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(productService).countAll();
    }

    @Test
    void shouldReturnApplicationInfoWithoutBuildProperties() throws Exception {
        // Given
        when(productService.countAll()).thenReturn(75L);

        // When & Then
        mockMvc.perform(get("/api/health/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("Spreadsheet Generator"))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.statistics.totalProducts").value(75))
                .andExpect(jsonPath("$.version").doesNotExist())
                .andExpect(jsonPath("$.buildTime").doesNotExist());
    }

    @Test
    void shouldReturnApplicationInfoWithServiceError() throws Exception {
        // Given
        when(productService.countAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/health/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("Spreadsheet Generator"))
                .andExpect(jsonPath("$.statistics.totalProducts").value("ERRO: Database error"));
    }

    @Test
    void shouldReturnCompleteJvmInformation() throws Exception {
        // Given
        when(productService.countAll()).thenReturn(200L);

        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.jvm.status").value("UP"))
                .andExpect(jsonPath("$.components.jvm.javaVersion").exists())
                .andExpect(jsonPath("$.components.jvm.javaVendor").exists())
                .andExpect(jsonPath("$.components.jvm.memory.totalMemoryMB").exists())
                .andExpect(jsonPath("$.components.jvm.memory.freeMemoryMB").exists())
                .andExpect(jsonPath("$.components.jvm.memory.maxMemoryMB").exists())
                .andExpect(jsonPath("$.components.jvm.memory.usedMemoryMB").exists())
                .andExpect(jsonPath("$.components.jvm.availableProcessors").exists());
    }

    @Test
    void shouldWorkWithCompleteMetadata() throws Exception {
        // Given
        when(productService.countAll()).thenReturn(300L);

        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.database.database").value("H2"))
                .andExpect(jsonPath("$.components.database.driver").value("H2 JDBC Driver"))
                .andExpect(jsonPath("$.components.database.url").value("jdbc:h2:mem:test"));

        verify(mockMetaData).getDatabaseProductName();
        verify(mockMetaData).getDriverName();
        verify(mockMetaData).getURL();
    }
} 