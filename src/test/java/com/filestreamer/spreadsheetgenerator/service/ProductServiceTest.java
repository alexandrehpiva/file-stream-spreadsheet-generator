package com.filestreamer.spreadsheetgenerator.service;

import com.filestreamer.spreadsheetgenerator.dto.ProductCreateDto;
import com.filestreamer.spreadsheetgenerator.dto.ProductDto;
import com.filestreamer.spreadsheetgenerator.dto.ProductUpdateDto;
import com.filestreamer.spreadsheetgenerator.exception.ProductNotFoundException;
import com.filestreamer.spreadsheetgenerator.model.Product;
import com.filestreamer.spreadsheetgenerator.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ProductService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductCreateDto productCreateDto;
    private ProductUpdateDto productUpdateDto;

    @BeforeEach
    void setUp() {
        UUID productId = UUID.randomUUID();
        
        product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setDescription("Test description");
        product.setPrice(BigDecimal.valueOf(99.99));
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        productCreateDto = new ProductCreateDto();
        productCreateDto.setName("Test Product");
        productCreateDto.setDescription("Test description");
        productCreateDto.setPrice(BigDecimal.valueOf(99.99));

        productUpdateDto = new ProductUpdateDto();
        productUpdateDto.setName("Updated Product");
        productUpdateDto.setDescription("Updated description");
        productUpdateDto.setPrice(BigDecimal.valueOf(149.99));
    }

    @Test
    @DisplayName("Deve criar produto com sucesso")
    void shouldCreateProductSuccessfully() {
        // Given
        when(productRepository.existsByNameIgnoreCase(productCreateDto.getName())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductDto result = productService.createProduct(productCreateDto);

        // Then
        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
        assertEquals(product.getDescription(), result.getDescription());
        assertEquals(product.getPrice(), result.getPrice());
        
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve buscar produto por ID com sucesso")
    void shouldFindProductByIdSuccessfully() {
        // Given
        UUID productId = product.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        ProductDto result = productService.findById(productId);

        // Then
        assertNotNull(result);
        assertEquals(product.getId(), result.getId());
        assertEquals(product.getName(), result.getName());
        
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado por ID")
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> 
            productService.findById(nonExistentId));
        
        verify(productRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Deve listar produtos com paginação")
    void shouldListProductsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = List.of(product);
        Page<Product> productsPage = new PageImpl<>(products, pageable, 1);
        
        when(productRepository.findAll(pageable)).thenReturn(productsPage);

        // When
        Page<ProductDto> result = productService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        
        verify(productRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve atualizar produto com sucesso")
    void shouldUpdateProductSuccessfully() {
        // Given
        UUID productId = product.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productRepository.existsByNameIgnoreCase(productUpdateDto.getName())).thenReturn(false);

        // When
        ProductDto result = productService.updateProduct(productId, productUpdateDto);

        // Then
        assertNotNull(result);
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve remover produto com sucesso")
    void shouldRemoveProductSuccessfully() {
        // Given
        UUID productId = product.getId();
        when(productRepository.existsById(productId)).thenReturn(true);

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository).existsById(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar remover produto inexistente")
    void shouldThrowExceptionWhenRemovingNonExistentProduct() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(productRepository.existsById(nonExistentId)).thenReturn(false);

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> 
            productService.deleteProduct(nonExistentId));
        
        verify(productRepository).existsById(nonExistentId);
        verify(productRepository, never()).deleteById(any());
    }
} 