package com.filestreamer.spreadsheetgenerator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filestreamer.spreadsheetgenerator.dto.ProductCreateDto;
import com.filestreamer.spreadsheetgenerator.dto.ProductDto;
import com.filestreamer.spreadsheetgenerator.dto.ProductUpdateDto;
import com.filestreamer.spreadsheetgenerator.exception.ProductNotFoundException;
import com.filestreamer.spreadsheetgenerator.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDto productDto;
    private ProductCreateDto createDto;
    private ProductUpdateDto updateDto;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        
        productDto = new ProductDto();
        productDto.setId(productId);
        productDto.setName("Test Product");
        productDto.setDescription("Test product description");
        productDto.setPrice(BigDecimal.valueOf(99.99));
        productDto.setCreatedAt(LocalDateTime.now());
        productDto.setUpdatedAt(LocalDateTime.now());

        createDto = new ProductCreateDto();
        createDto.setName("New Product");
        createDto.setDescription("New product description");
        createDto.setPrice(BigDecimal.valueOf(149.99));

        updateDto = new ProductUpdateDto();
        updateDto.setName("Updated Product");
        updateDto.setDescription("Updated description");
        updateDto.setPrice(BigDecimal.valueOf(199.99));
    }

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        // Given
        when(productService.createProduct(any(ProductCreateDto.class))).thenReturn(productDto);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));

        verify(productService).createProduct(any(ProductCreateDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreationFails() throws Exception {
        // Given
        when(productService.createProduct(any(ProductCreateDto.class)))
                .thenThrow(new IllegalArgumentException("Nome já existe"));

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());

        verify(productService).createProduct(any(ProductCreateDto.class));
    }

    @Test
    void shouldFindProductByIdSuccessfully() throws Exception {
        // Given
        when(productService.findById(productId)).thenReturn(productDto);

        // When & Then
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService).findById(productId);
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        // Given
        when(productService.findById(productId))
                .thenThrow(new ProductNotFoundException("Product not found"));

        // When & Then
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound());

        verify(productService).findById(productId);
    }

    @Test
    void shouldListProductsWithPagination() throws Exception {
        // Given
        List<ProductDto> products = List.of(productDto);
        Page<ProductDto> page = new PageImpl<>(products);
        when(productService.findAll(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].id").value(productId.toString()))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false));

        verify(productService).findAll(any(Pageable.class));
    }

    @Test
    void shouldListProductsWithDefaultParameters() throws Exception {
        // Given
        List<ProductDto> products = List.of(productDto);
        Page<ProductDto> page = new PageImpl<>(products);
        when(productService.findAll(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray());

        verify(productService).findAll(any(Pageable.class));
    }

    @Test
    void shouldReturnInternalServerErrorWhenListingFails() throws Exception {
        // Given
        when(productService.findAll(any(Pageable.class)))
                .thenThrow(new RuntimeException("Erro interno"));

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isInternalServerError());

        verify(productService).findAll(any(Pageable.class));
    }

    @Test
    void shouldFindProductsByName() throws Exception {
        // Given
        List<ProductDto> products = List.of(productDto);
        when(productService.findByName("Test")).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                        .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        verify(productService).findByName("Test");
    }

    @Test
    void shouldReturnBadRequestWhenNameIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products/search")
                        .param("name", "   "))
                .andExpect(status().isBadRequest());

        verify(productService, never()).findByName(anyString());
    }

    @Test
    void shouldFindProductsByPriceRange() throws Exception {
        // Given
        List<ProductDto> products = List.of(productDto);
        when(productService.findByPriceRange(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/price-range")
                        .param("priceMin", "50.00")
                        .param("priceMax", "150.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        // Verificar que foi chamado com qualquer BigDecimal (devido à conversão automática do Spring)
        verify(productService).findByPriceRange(any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    void shouldReturnBadRequestWhenPriceRangeIsInvalid() throws Exception {
        // Given
        when(productService.findByPriceRange(any(BigDecimal.class), any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Preço mínimo maior que máximo"));

        // When & Then
        mockMvc.perform(get("/api/products/price-range")
                        .param("priceMin", "150.00")
                        .param("priceMax", "50.00"))
                .andExpect(status().isBadRequest());

        verify(productService).findByPriceRange(any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    void shouldUpdateProductSuccessfully() throws Exception {
        // Given
        when(productService.updateProduct(eq(productId), any(ProductUpdateDto.class)))
                .thenReturn(productDto);

        // When & Then
        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()));

        verify(productService).updateProduct(eq(productId), any(ProductUpdateDto.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdateFailsProductNotFound() throws Exception {
        // Given
        when(productService.updateProduct(eq(productId), any(ProductUpdateDto.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        // When & Then
        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(productService).updateProduct(eq(productId), any(ProductUpdateDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateFailsIllegalArgument() throws Exception {
        // Given
        when(productService.updateProduct(eq(productId), any(ProductUpdateDto.class)))
                .thenThrow(new IllegalArgumentException("Nome já existe"));

        // When & Then
        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());

        verify(productService).updateProduct(eq(productId), any(ProductUpdateDto.class));
    }

    @Test
    void shouldDeleteProductSuccessfully() throws Exception {
        // Given
        doNothing().when(productService).deleteProduct(productId);

        // When & Then
        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(productId);
    }

    @Test
    void shouldReturnNotFoundWhenDeleteProductDoesNotExist() throws Exception {
        // Given
        doThrow(new ProductNotFoundException("Product not found"))
                .when(productService).deleteProduct(productId);

        // When & Then
        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNotFound());

        verify(productService).deleteProduct(productId);
    }

    @Test
    void shouldCountProducts() throws Exception {
        // Given
        when(productService.countAll()).thenReturn(100L);

        // When & Then
        mockMvc.perform(get("/api/products/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(100));

        verify(productService).countAll();
    }

    @Test
    void shouldHandleValidationException() throws Exception {
        // Given
        when(productService.createProduct(any(ProductCreateDto.class)))
                .thenThrow(new IllegalArgumentException("Erro de validação"));

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleProductNotFoundException() throws Exception {
        // Given
        when(productService.findById(productId))
                .thenThrow(new ProductNotFoundException("Product not found"));

        // When & Then
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound());

        verify(productService).findById(productId);
    }
} 