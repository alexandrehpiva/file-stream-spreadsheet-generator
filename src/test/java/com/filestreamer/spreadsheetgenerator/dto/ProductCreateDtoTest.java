package com.filestreamer.spreadsheetgenerator.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductCreateDtoTest {

    @Test
    void shouldCreateProductCreateDtoWithDefaultConstructor() {
        // Given & When
        ProductCreateDto createDto = new ProductCreateDto();

        // Then
        assertNotNull(createDto);
        assertNull(createDto.getName());
        assertNull(createDto.getDescription());
        assertNull(createDto.getPrice());
    }

    @Test
    void shouldCreateProductCreateDtoWithParameterizedConstructor() {
        // Given
        String name = "Test Product";
        String description = "Test Description";
        BigDecimal price = BigDecimal.valueOf(99.99);

        // When
        ProductCreateDto createDto = new ProductCreateDto(name, description, price);

        // Then
        assertNotNull(createDto);
        assertEquals(name, createDto.getName());
        assertEquals(description, createDto.getDescription());
        assertEquals(price, createDto.getPrice());
    }

    @Test
    void shouldSetAndGetName() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();
        String name = "Smartphone";

        // When
        createDto.setName(name);

        // Then
        assertEquals(name, createDto.getName());
    }

    @Test
    void shouldSetAndGetDescription() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();
        String description = "High-quality smartphone with great features";

        // When
        createDto.setDescription(description);

        // Then
        assertEquals(description, createDto.getDescription());
    }

    @Test
    void shouldSetAndGetPrice() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();
        BigDecimal price = BigDecimal.valueOf(1299.99);

        // When
        createDto.setPrice(price);

        // Then
        assertEquals(price, createDto.getPrice());
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();

        // When
        createDto.setName(null);
        createDto.setDescription(null);
        createDto.setPrice(null);

        // Then
        assertNull(createDto.getName());
        assertNull(createDto.getDescription());
        assertNull(createDto.getPrice());
    }

    @Test
    void shouldHandleEmptyStringValues() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();

        // When
        createDto.setName("");
        createDto.setDescription("");

        // Then
        assertEquals("", createDto.getName());
        assertEquals("", createDto.getDescription());
    }

    @Test
    void shouldHandleWhitespaceValues() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();

        // When
        createDto.setName("   ");
        createDto.setDescription("   ");

        // Then
        assertEquals("   ", createDto.getName());
        assertEquals("   ", createDto.getDescription());
    }

    @Test
    void shouldHandleZeroPrice() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();
        BigDecimal zeroPrice = BigDecimal.ZERO;

        // When
        createDto.setPrice(zeroPrice);

        // Then
        assertEquals(zeroPrice, createDto.getPrice());
    }

    @Test
    void shouldHandleNegativePrice() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();
        BigDecimal negativePrice = BigDecimal.valueOf(-10.50);

        // When
        createDto.setPrice(negativePrice);

        // Then
        assertEquals(negativePrice, createDto.getPrice());
    }

    @Test
    void shouldHandleLargePrice() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();
        BigDecimal largePrice = BigDecimal.valueOf(999999.99);

        // When
        createDto.setPrice(largePrice);

        // Then
        assertEquals(largePrice, createDto.getPrice());
    }

    @Test
    void shouldHandlePreciseDecimalPrice() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();
        BigDecimal precisePrice = new BigDecimal("99.999");

        // When
        createDto.setPrice(precisePrice);

        // Then
        assertEquals(precisePrice, createDto.getPrice());
    }

    @Test
    void shouldMaintainIndependentFieldValues() {
        // Given
        ProductCreateDto createDto1 = new ProductCreateDto();
        ProductCreateDto createDto2 = new ProductCreateDto();
        
        String name1 = "Product 1";
        String name2 = "Product 2";
        BigDecimal price1 = BigDecimal.valueOf(100.00);
        BigDecimal price2 = BigDecimal.valueOf(200.00);

        // When
        createDto1.setName(name1);
        createDto1.setPrice(price1);
        createDto2.setName(name2);
        createDto2.setPrice(price2);

        // Then
        assertNotEquals(createDto1.getName(), createDto2.getName());
        assertNotEquals(createDto1.getPrice(), createDto2.getPrice());
        assertEquals(name1, createDto1.getName());
        assertEquals(name2, createDto2.getName());
        assertEquals(price1, createDto1.getPrice());
        assertEquals(price2, createDto2.getPrice());
    }

    @Test
    void shouldAllowLongDescriptions() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();
        String longDescription = "This is a very long description that contains many details about the product. ".repeat(10);

        // When
        createDto.setDescription(longDescription);

        // Then
        assertEquals(longDescription, createDto.getDescription());
    }

    @Test
    void shouldAllowSpecialCharactersInName() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();
        String nameWithSpecialChars = "Product-Name_123 & More!";

        // When
        createDto.setName(nameWithSpecialChars);

        // Then
        assertEquals(nameWithSpecialChars, createDto.getName());
    }

    @Test
    void shouldAllowUnicodeCharacters() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();
        String unicodeName = "Produto Açaí 中文";

        // When
        createDto.setName(unicodeName);

        // Then
        assertEquals(unicodeName, createDto.getName());
    }
} 