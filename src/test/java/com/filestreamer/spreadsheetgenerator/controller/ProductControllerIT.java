package com.filestreamer.spreadsheetgenerator.controller;

import com.filestreamer.spreadsheetgenerator.dto.ProductCreateDto;
import com.filestreamer.spreadsheetgenerator.dto.ProductDto;
import com.filestreamer.spreadsheetgenerator.dto.ProductUpdateDto;
import com.filestreamer.spreadsheetgenerator.model.Product;
import com.filestreamer.spreadsheetgenerator.repository.ProductRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para ProductController.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("ProductController Integration Tests")
class ProductControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    private String baseUrl;
    private Product product;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/products";
        
        // Limpar dados antes de cada teste
        productRepository.deleteAll();
        
        // Criar produto base para testes
        product = new Product("Produto Teste", "Descrição teste", BigDecimal.valueOf(99.99));
        product = productRepository.save(product);
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar produto via POST /api/products")
    void deveCriarProductViaPOST() {
        // Given
        ProductCreateDto createDto = new ProductCreateDto();
        createDto.setName("Novo Produto");
        createDto.setDescription("Nova descrição");
        createDto.setPrice(BigDecimal.valueOf(149.99));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductCreateDto> request = new HttpEntity<>(createDto, headers);

        // When
        ResponseEntity<ProductDto> response = restTemplate.postForEntity(baseUrl, request, ProductDto.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProductDto responseBody = response.getBody();
        assertNotNull(responseBody.getId());
        assertEquals("Novo Produto", responseBody.getName());
        assertEquals("Nova descrição", responseBody.getDescription());
        assertEquals(BigDecimal.valueOf(149.99), responseBody.getPrice());
    }

    @Test
    @DisplayName("Deve buscar produto por ID via GET /api/products/{id}")
    void deveBuscarProductPorIdViaGET() {
        // When
        ResponseEntity<ProductDto> response = restTemplate.getForEntity(
            baseUrl + "/" + product.getId(), ProductDto.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProductDto responseBody = response.getBody();
        assertEquals(product.getId(), responseBody.getId());
        assertEquals(product.getName(), responseBody.getName());
        assertEquals(product.getDescription(), responseBody.getDescription());
        assertEquals(product.getPrice(), responseBody.getPrice());
    }

    @Test
    @DisplayName("Deve retornar 404 para produto inexistente")
    void deveRetornar404ParaProductInexistente() {
        // Given
        UUID idInexistente = UUID.randomUUID();

        // When
        ResponseEntity<ProductDto> response = restTemplate.getForEntity(
            baseUrl + "/" + idInexistente, ProductDto.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve listar produtos com paginação via GET /api/products")
    void deveListarProductsComPaginacaoViaGET() {
        // Given - Criar um produto para garantir que há dados
        ProductCreateDto createDto = new ProductCreateDto();
        createDto.setName("Produto para Listagem");
        createDto.setDescription("Descrição para teste de listagem");
        createDto.setPrice(BigDecimal.valueOf(29.99));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductCreateDto> createRequest = new HttpEntity<>(createDto, headers);
        restTemplate.postForEntity(baseUrl, createRequest, ProductDto.class);

        // When
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            baseUrl + "?page=0&size=5",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verificar se contém as chaves corretas baseadas no controller
        assertTrue(response.getBody().containsKey("products"));
        assertTrue(response.getBody().containsKey("totalItems"));
        assertTrue(response.getBody().containsKey("totalPages"));
        assertTrue(response.getBody().containsKey("currentPage"));
        
        // Verificar que há pelo menos 2 produtos
        Object totalItems = response.getBody().get("totalItems");
        assertNotNull(totalItems);
        
        if (totalItems instanceof Number) {
            assertTrue(((Number) totalItems).longValue() >= 2);
        }
    }

    @Test
    @DisplayName("Deve atualizar produto via PUT /api/products/{id}")
    void deveAtualizarProductViaPUT() {
        // Given
        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setName("Produto Atualizado");
        updateDto.setDescription("Descrição atualizada");
        updateDto.setPrice(BigDecimal.valueOf(199.99));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductUpdateDto> request = new HttpEntity<>(updateDto, headers);

        // When
        ResponseEntity<ProductDto> response = restTemplate.exchange(
            baseUrl + "/" + product.getId(),
            HttpMethod.PUT,
            request,
            ProductDto.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProductDto responseBody = response.getBody();
        assertEquals(product.getId(), responseBody.getId());
        assertEquals("Produto Atualizado", responseBody.getName());
        assertEquals("Descrição atualizada", responseBody.getDescription());
        assertEquals(BigDecimal.valueOf(199.99), responseBody.getPrice());
    }

    @Test
    @DisplayName("Deve deletar produto via DELETE /api/products/{id}")
    void deveDeletarProductViaDELETE() {
        // When
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/" + product.getId(),
            HttpMethod.DELETE,
            null,
            Void.class
        );

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        // Verificar se o produto foi realmente deletado
        assertFalse(productRepository.existsById(product.getId()));
    }

    @Test
    @DisplayName("Deve validar dados obrigatórios ao criar produto")
    void deveValidarDadosObrigatoriosAoCriarProduct() {
        // Given - DTO com dados inválidos
        ProductCreateDto createDto = new ProductCreateDto();
        createDto.setName(""); // Nome vazio
        createDto.setDescription("Descrição válida");
        createDto.setPrice(BigDecimal.valueOf(-10.0)); // Preço negativo
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductCreateDto> request = new HttpEntity<>(createDto, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
} 