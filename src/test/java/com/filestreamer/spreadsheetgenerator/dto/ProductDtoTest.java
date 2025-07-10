package com.filestreamer.spreadsheetgenerator.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductDtoTest {

    @Test
    void shouldCreateProductDtoWithDefaultConstructor() {
        // Given & When
        ProductDto productDto = new ProductDto();

        // Then
        assertNotNull(productDto);
        assertNull(productDto.getId());
        assertNull(productDto.getName());
        assertNull(productDto.getDescription());
        assertNull(productDto.getPrice());
        assertNull(productDto.getCreatedAt());
        assertNull(productDto.getUpdatedAt());
    }

    @Test
    void shouldCreateProductDtoWithParameterizedConstructor() {
        // Given
        UUID id = UUID.randomUUID();
        String name = "Test Product";
        String description = "Test Description";
        BigDecimal price = BigDecimal.valueOf(99.99);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now().plusMinutes(5);

        // When
        ProductDto productDto = new ProductDto(id, name, description, price, createdAt, updatedAt);

        // Then
        assertNotNull(productDto);
        assertEquals(id, productDto.getId());
        assertEquals(name, productDto.getName());
        assertEquals(description, productDto.getDescription());
        assertEquals(price, productDto.getPrice());
        assertEquals(createdAt, productDto.getCreatedAt());
        assertEquals(updatedAt, productDto.getUpdatedAt());
    }

    @Test
    void shouldSetAndGetId() {
        // Given
        ProductDto productDto = new ProductDto();
        UUID id = UUID.randomUUID();

        // When
        productDto.setId(id);

        // Then
        assertEquals(id, productDto.getId());
    }

    @Test
    void shouldSetAndGetName() {
        // Given
        ProductDto productDto = new ProductDto();
        String name = "Smartphone";

        // When
        productDto.setName(name);

        // Then
        assertEquals(name, productDto.getName());
    }

    @Test
    void shouldSetAndGetDescription() {
        // Given
        ProductDto productDto = new ProductDto();
        String description = "High-quality smartphone with great features";

        // When
        productDto.setDescription(description);

        // Then
        assertEquals(description, productDto.getDescription());
    }

    @Test
    void shouldSetAndGetPrice() {
        // Given
        ProductDto productDto = new ProductDto();
        BigDecimal price = BigDecimal.valueOf(1299.99);

        // When
        productDto.setPrice(price);

        // Then
        assertEquals(price, productDto.getPrice());
    }

    @Test
    void shouldSetAndGetCreatedAt() {
        // Given
        ProductDto productDto = new ProductDto();
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        productDto.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, productDto.getCreatedAt());
    }

    @Test
    void shouldSetAndGetUpdatedAt() {
        // Given
        ProductDto productDto = new ProductDto();
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        productDto.setUpdatedAt(updatedAt);

        // Then
        assertEquals(updatedAt, productDto.getUpdatedAt());
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        ProductDto productDto = new ProductDto();

        // When
        productDto.setId(null);
        productDto.setName(null);
        productDto.setDescription(null);
        productDto.setPrice(null);
        productDto.setCreatedAt(null);
        productDto.setUpdatedAt(null);

        // Then
        assertNull(productDto.getId());
        assertNull(productDto.getName());
        assertNull(productDto.getDescription());
        assertNull(productDto.getPrice());
        assertNull(productDto.getCreatedAt());
        assertNull(productDto.getUpdatedAt());
    }

    @Test
    void shouldHandleEmptyStringValues() {
        // Given
        ProductDto productDto = new ProductDto();

        // When
        productDto.setName("");
        productDto.setDescription("");

        // Then
        assertEquals("", productDto.getName());
        assertEquals("", productDto.getDescription());
    }

    @Test
    void shouldHandleZeroPrice() {
        // Given
        ProductDto productDto = new ProductDto();
        BigDecimal zeroPrice = BigDecimal.ZERO;

        // When
        productDto.setPrice(zeroPrice);

        // Then
        assertEquals(zeroPrice, productDto.getPrice());
    }

    @Test
    void shouldHandleNegativePrice() {
        // Given
        ProductDto productDto = new ProductDto();
        BigDecimal negativePrice = BigDecimal.valueOf(-10.50);

        // When
        productDto.setPrice(negativePrice);

        // Then
        assertEquals(negativePrice, productDto.getPrice());
    }

    @Test
    void shouldHandleLargePrice() {
        // Given
        ProductDto productDto = new ProductDto();
        BigDecimal largePrice = BigDecimal.valueOf(999999.99);

        // When
        productDto.setPrice(largePrice);

        // Then
        assertEquals(largePrice, productDto.getPrice());
    }

    @Test
    void shouldMaintainIndependentFieldValues() {
        // Given
        ProductDto productDto1 = new ProductDto();
        ProductDto productDto2 = new ProductDto();
        
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        String name1 = "Product 1";
        String name2 = "Product 2";

        // When
        productDto1.setId(id1);
        productDto1.setName(name1);
        productDto2.setId(id2);
        productDto2.setName(name2);

        // Then
        assertNotEquals(productDto1.getId(), productDto2.getId());
        assertNotEquals(productDto1.getName(), productDto2.getName());
        assertEquals(id1, productDto1.getId());
        assertEquals(id2, productDto2.getId());
        assertEquals(name1, productDto1.getName());
        assertEquals(name2, productDto2.getName());
    }
} 