package com.filestreamer.spreadsheetgenerator.service.export;

import com.filestreamer.spreadsheetgenerator.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ProductDataFormatterTest {

    private ProductDataFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new ProductDataFormatter();
    }

    @Test
    void testFormatToRow() {
        // Given
        UUID productId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2024, 6, 23, 14, 30, 22);
        
        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setDescription("Test product description");
        product.setPrice(new BigDecimal("99.99"));
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        // When
        String[] row = formatter.formatToRow(product);

        // Then
        assertNotNull(row);
        assertEquals(6, row.length);
        assertEquals(productId.toString(), row[0]);
        assertEquals("Test Product", row[1]);
        assertEquals("Test product description", row[2]);
        assertEquals("99.99", row[3]);
        assertEquals("2024-06-23 14:30:22", row[4]);
        assertEquals("2024-06-23 14:30:22", row[5]);
    }

    @Test
    void testFormatToRowWithNullDescription() {
        // Given
        UUID productId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2024, 6, 23, 14, 30, 22);
        
        Product product = new Product();
        product.setId(productId);
        product.setName("Product Without Description");
        product.setDescription(null); // Descrição nula
        product.setPrice(new BigDecimal("50.00"));
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        // When
        String[] row = formatter.formatToRow(product);

        // Then
        assertNotNull(row);
        assertEquals(6, row.length);
        assertEquals(productId.toString(), row[0]);
        assertEquals("Product Without Description", row[1]);
        assertEquals("", row[2]); // Descrição vazia quando null
        assertEquals("50.00", row[3]);
        assertEquals("2024-06-23 14:30:22", row[4]);
        assertEquals("2024-06-23 14:30:22", row[5]);
    }

    @Test
    void testGetHeaders() {
        // When
        String[] headers = formatter.getHeaders();

        // Then
        assertNotNull(headers);
        assertEquals(6, headers.length);
        assertEquals("ID", headers[0]);
        assertEquals("Nome", headers[1]);
        assertEquals("Descrição", headers[2]);
        assertEquals("Preço", headers[3]);
        assertEquals("Data Criação", headers[4]);
        assertEquals("Data Atualização", headers[5]);
    }

    @Test
    void testFormatToRows() {
        // Given
        LocalDateTime now = LocalDateTime.of(2024, 6, 23, 14, 30, 22);
        
        Product product1 = createProduct(UUID.randomUUID(), "Product 1", "Desc 1", "10.00", now);
        Product product2 = createProduct(UUID.randomUUID(), "Product 2", "Desc 2", "20.00", now);
        Product product3 = createProduct(UUID.randomUUID(), "Product 3", null, "30.00", now);
        
        Stream<Product> productStream = Stream.of(product1, product2, product3);

        // When
        List<String[]> rows = formatter.formatToRows(productStream).toList();

        // Then
        assertNotNull(rows);
        assertEquals(3, rows.size());
        
        // Verificar primeira linha
        String[] row1 = rows.get(0);
        assertEquals(product1.getId().toString(), row1[0]);
        assertEquals("Product 1", row1[1]);
        assertEquals("Desc 1", row1[2]);
        assertEquals("10.00", row1[3]);
        
        // Verificar segunda linha
        String[] row2 = rows.get(1);
        assertEquals(product2.getId().toString(), row2[0]);
        assertEquals("Product 2", row2[1]);
        assertEquals("Desc 2", row2[2]);
        assertEquals("20.00", row2[3]);
        
        // Verificar terceira linha (com descrição null)
        String[] row3 = rows.get(2);
        assertEquals(product3.getId().toString(), row3[0]);
        assertEquals("Product 3", row3[1]);
        assertEquals("", row3[2]); // Descrição vazia
        assertEquals("30.00", row3[3]);
    }

    @Test
    void testFormatToRowsWithEmptyStream() {
        // Given
        Stream<Product> emptyStream = Stream.empty();

        // When
        List<String[]> rows = formatter.formatToRows(emptyStream).toList();

        // Then
        assertNotNull(rows);
        assertTrue(rows.isEmpty());
    }

    // Helper method
    private Product createProduct(UUID id, String name, String description, String price, LocalDateTime dateTime) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setCreatedAt(dateTime);
        product.setUpdatedAt(dateTime);
        return product;
    }
} 