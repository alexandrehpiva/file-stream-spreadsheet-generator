package com.filestreamer.spreadsheetgenerator.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductUpdateDtoTest {

    @Test
    void shouldCreateProductUpdateDtoWithDefaultConstructor() {
        // Given & When
        ProductUpdateDto updateDto = new ProductUpdateDto();

        // Then
        assertNotNull(updateDto);
        assertNull(updateDto.getName());
        assertNull(updateDto.getDescription());
        assertNull(updateDto.getPrice());
    }

    @Test
    void shouldCreateProductUpdateDtoWithParameterizedConstructor() {
        // Given
        String name = "Updated Product";
        String description = "Updated Description";
        BigDecimal price = BigDecimal.valueOf(149.99);

        // When
        ProductUpdateDto updateDto = new ProductUpdateDto(name, description, price);

        // Then
        assertNotNull(updateDto);
        assertEquals(name, updateDto.getName());
        assertEquals(description, updateDto.getDescription());
        assertEquals(price, updateDto.getPrice());
    }

    @Test
    void shouldSetAndGetName() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        String name = "Updated Smartphone";

        // When
        updateDto.setName(name);

        // Then
        assertEquals(name, updateDto.getName());
    }

    @Test
    void shouldSetAndGetDescription() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        String description = "Updated high-quality smartphone with enhanced features";

        // When
        updateDto.setDescription(description);

        // Then
        assertEquals(description, updateDto.getDescription());
    }

    @Test
    void shouldSetAndGetPrice() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        BigDecimal price = BigDecimal.valueOf(1599.99);

        // When
        updateDto.setPrice(price);

        // Then
        assertEquals(price, updateDto.getPrice());
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();

        // When
        updateDto.setName(null);
        updateDto.setDescription(null);
        updateDto.setPrice(null);

        // Then
        assertNull(updateDto.getName());
        assertNull(updateDto.getDescription());
        assertNull(updateDto.getPrice());
    }

    @Test
    void shouldHandleEmptyStringValues() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();

        // When
        updateDto.setName("");
        updateDto.setDescription("");

        // Then
        assertEquals("", updateDto.getName());
        assertEquals("", updateDto.getDescription());
    }

    @Test
    void shouldHandleWhitespaceValues() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();

        // When
        updateDto.setName("   ");
        updateDto.setDescription("   ");

        // Then
        assertEquals("   ", updateDto.getName());
        assertEquals("   ", updateDto.getDescription());
    }

    @Test
    void shouldHandleZeroPrice() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        BigDecimal zeroPrice = BigDecimal.ZERO;

        // When
        updateDto.setPrice(zeroPrice);

        // Then
        assertEquals(zeroPrice, updateDto.getPrice());
    }

    @Test
    void shouldHandleNegativePrice() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        BigDecimal negativePrice = BigDecimal.valueOf(-5.75);

        // When
        updateDto.setPrice(negativePrice);

        // Then
        assertEquals(negativePrice, updateDto.getPrice());
    }

    @Test
    void shouldHandleLargePrice() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        BigDecimal largePrice = BigDecimal.valueOf(50000.00);

        // When
        updateDto.setPrice(largePrice);

        // Then
        assertEquals(largePrice, updateDto.getPrice());
    }

    @Test
    void shouldHandlePreciseDecimalPrice() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        BigDecimal precisePrice = new BigDecimal("199.995");

        // When
        updateDto.setPrice(precisePrice);

        // Then
        assertEquals(precisePrice, updateDto.getPrice());
    }

    @Test
    void shouldMaintainIndependentFieldValues() {
        // Given
        ProductUpdateDto updateDto1 = new ProductUpdateDto();
        ProductUpdateDto updateDto2 = new ProductUpdateDto();
        
        String name1 = "Updated Product 1";
        String name2 = "Updated Product 2";
        BigDecimal price1 = BigDecimal.valueOf(150.00);
        BigDecimal price2 = BigDecimal.valueOf(250.00);

        // When
        updateDto1.setName(name1);
        updateDto1.setPrice(price1);
        updateDto2.setName(name2);
        updateDto2.setPrice(price2);

        // Then
        assertNotEquals(updateDto1.getName(), updateDto2.getName());
        assertNotEquals(updateDto1.getPrice(), updateDto2.getPrice());
        assertEquals(name1, updateDto1.getName());
        assertEquals(name2, updateDto2.getName());
        assertEquals(price1, updateDto1.getPrice());
        assertEquals(price2, updateDto2.getPrice());
    }

    @Test
    void shouldAllowLongDescriptions() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        String longDescription = "This is a very long updated description that contains many details about the product improvements and new features. ".repeat(5);

        // When
        updateDto.setDescription(longDescription);

        // Then
        assertEquals(longDescription, updateDto.getDescription());
    }

    @Test
    void shouldAllowSpecialCharactersInName() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        String nameWithSpecialChars = "Updated-Product_v2.1 & More!";

        // When
        updateDto.setName(nameWithSpecialChars);

        // Then
        assertEquals(nameWithSpecialChars, updateDto.getName());
    }

    @Test
    void shouldAllowUnicodeCharacters() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        String unicodeName = "Produto Atualizado Açaí 中文";

        // When
        updateDto.setName(unicodeName);

        // Then
        assertEquals(unicodeName, updateDto.getName());
    }

    @Test
    void shouldPreserveOriginalValueWhenNotUpdated() {
        // Given
        String originalName = "Original Product";
        String originalDescription = "Original Description";
        BigDecimal originalPrice = BigDecimal.valueOf(100.00);
        
        ProductUpdateDto updateDto = new ProductUpdateDto(originalName, originalDescription, originalPrice);

        // When
        // Only update the name
        updateDto.setName("Updated Name");

        // Then
        assertEquals("Updated Name", updateDto.getName());
        assertEquals(originalDescription, updateDto.getDescription());
        assertEquals(originalPrice, updateDto.getPrice());
    }

    @Test
    void shouldAllowPartialUpdates() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();

        // When
        updateDto.setName("Only Name Updated");
        // Description and price remain null

        // Then
        assertEquals("Only Name Updated", updateDto.getName());
        assertNull(updateDto.getDescription());
        assertNull(updateDto.getPrice());
    }
} 