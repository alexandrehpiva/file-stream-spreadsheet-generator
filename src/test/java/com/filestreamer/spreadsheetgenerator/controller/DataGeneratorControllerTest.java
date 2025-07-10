package com.filestreamer.spreadsheetgenerator.controller;

import com.filestreamer.spreadsheetgenerator.service.DataGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataGeneratorController.class)
class DataGeneratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataGeneratorService dataGeneratorService;

    @BeforeEach
    void setUp() {
        // Setup comum se necessário
    }

    @Test
    void shouldGenerateProductsWithValidQuantity() throws Exception {
        // Given
        int quantity = 100;
        when(dataGeneratorService.getProductCount())
                .thenReturn(50L)  // Contagem inicial
                .thenReturn(150L); // Contagem final
        
        // When & Then
        mockMvc.perform(post("/api/data-generator/products/{quantity}", quantity))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Produtos gerados com sucesso"))
                .andExpect(jsonPath("$.generated_quantity").value(100))
                .andExpect(jsonPath("$.execution_time_ms").exists())
                .andExpect(jsonPath("$.products_per_second").exists())
                .andExpect(jsonPath("$.initial_count").value(50))
                .andExpect(jsonPath("$.final_count").value(150))
                .andExpect(jsonPath("$.total_products_in_database").value(150));

        verify(dataGeneratorService).generateRandomProducts(quantity);
        verify(dataGeneratorService, times(2)).getProductCount();
    }

    @Test
    void shouldGenerateProductsWithMaximumQuantity() throws Exception {
        // Given
        int quantity = 1_000_000;
        when(dataGeneratorService.getProductCount())
                .thenReturn(0L)  // Contagem inicial
                .thenReturn(1_000_000L); // Contagem final
        
        // When & Then
        mockMvc.perform(post("/api/data-generator/products/{quantity}", quantity))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Produtos gerados com sucesso"))
                .andExpect(jsonPath("$.generated_quantity").value(1_000_000));

        verify(dataGeneratorService).generateRandomProducts(quantity);
    }

    @Test
    void shouldReturnBadRequestForZeroQuantity() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/data-generator/products/{quantity}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Quantidade deve estar entre 1 e 1.000.000"))
                .andExpect(jsonPath("$.requested_quantity").value(0));

        verify(dataGeneratorService, never()).generateRandomProducts(anyInt());
    }

    @Test
    void shouldReturnBadRequestForNegativeQuantity() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/data-generator/products/{quantity}", -1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Quantidade deve estar entre 1 e 1.000.000"))
                .andExpect(jsonPath("$.requested_quantity").value(-1));

        verify(dataGeneratorService, never()).generateRandomProducts(anyInt());
    }

    @Test
    void shouldReturnBadRequestForQuantityAboveLimit() throws Exception {
        // Given
        int quantityOverLimit = 1_000_001;
        
        // When & Then
        mockMvc.perform(post("/api/data-generator/products/{quantity}", quantityOverLimit))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Quantidade deve estar entre 1 e 1.000.000"))
                .andExpect(jsonPath("$.requested_quantity").value(quantityOverLimit));

        verify(dataGeneratorService, never()).generateRandomProducts(anyInt());
    }

    @Test
    void shouldClearAllProducts() throws Exception {
        // Given 
        when(dataGeneratorService.getProductCount()).thenReturn(500L);
        
        // When & Then
        mockMvc.perform(delete("/api/data-generator/products/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Todos os produtos foram removidos"))
                .andExpect(jsonPath("$.removed_count").value(500))
                .andExpect(jsonPath("$.remaining_count").value(0));

        verify(dataGeneratorService).getProductCount();
        verify(dataGeneratorService).clearAllProducts();
    }

    @Test
    void shouldClearAllProductsWhenNoRecordsExist() throws Exception {
        // Given 
        when(dataGeneratorService.getProductCount()).thenReturn(0L);
        
        // When & Then
        mockMvc.perform(delete("/api/data-generator/products/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Todos os produtos foram removidos"))
                .andExpect(jsonPath("$.removed_count").value(0))
                .andExpect(jsonPath("$.remaining_count").value(0));

        verify(dataGeneratorService).getProductCount();
        verify(dataGeneratorService).clearAllProducts();
    }

    @Test
    void shouldReturnGeneratorInformation() throws Exception {
        // Given
        when(dataGeneratorService.getProductCount()).thenReturn(250L);
        
        // When & Then
        mockMvc.perform(get("/api/data-generator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("Data Generator API"))
                .andExpect(jsonPath("$.description").value("Gerador de dados aleatórios para produtos"))
                .andExpect(jsonPath("$.max_quantity_per_request").value(1_000_000))
                .andExpect(jsonPath("$.batch_size").value(1_000))
                .andExpect(jsonPath("$.features").isArray())
                .andExpect(jsonPath("$.features").value(org.hamcrest.Matchers.hasSize(7)))
                .andExpect(jsonPath("$.current_products_count").value(250));

        verify(dataGeneratorService).getProductCount();
    }

    @Test
    void shouldCalculateProductsPerSecondCorrectly() throws Exception {
        // Given - Simula geração com tempo específico
        int quantity = 1000;
        when(dataGeneratorService.getProductCount())
                .thenReturn(100L)   // Inicial
                .thenReturn(1100L); // Final
        
        // When & Then
        mockMvc.perform(post("/api/data-generator/products/{quantity}", quantity))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generated_quantity").value(1000))
                .andExpect(jsonPath("$.products_per_second").exists());

        verify(dataGeneratorService).generateRandomProducts(quantity);
    }

    @Test
    void shouldProcessGenerationWithMinimumQuantity() throws Exception {
        // Given
        int quantity = 1;
        when(dataGeneratorService.getProductCount())
                .thenReturn(999L)  // Inicial  
                .thenReturn(1000L); // Final
        
        // When & Then
        mockMvc.perform(post("/api/data-generator/products/{quantity}", quantity))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generated_quantity").value(1))
                .andExpect(jsonPath("$.initial_count").value(999))
                .andExpect(jsonPath("$.final_count").value(1000));

        verify(dataGeneratorService).generateRandomProducts(1);
    }
} 