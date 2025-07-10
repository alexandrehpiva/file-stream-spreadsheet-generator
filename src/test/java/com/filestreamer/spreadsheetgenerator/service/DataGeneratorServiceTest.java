package com.filestreamer.spreadsheetgenerator.service;

import com.filestreamer.spreadsheetgenerator.model.Product;
import com.filestreamer.spreadsheetgenerator.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataGeneratorServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DataGeneratorService dataGeneratorService;

    @BeforeEach
    void setUp() {
        // Setup comum para todos os testes
    }

    @Test
    void shouldGenerateRandomProductsSuccessfully() {
        // Given
        int quantity = 100;
        when(productRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        dataGeneratorService.generateRandomProducts(quantity);

        // Then
        // Verifica se saveAll foi chamado pelo menos uma vez (batches de 1000)
        verify(productRepository, atLeastOnce()).saveAll(anyList());
        
        // Captura todas as chamadas para verificar o total de produtos
        List<List<Product>> allBatches = captureAllSaveAllCalls();
        int totalProductsGenerated = allBatches.stream()
                .mapToInt(List::size)
                .sum();
        
        assertEquals(quantity, totalProductsGenerated);
    }

    @Test
    void shouldGenerateProductsInBatchesWhenQuantityGreaterThan1000() {
        // Given
        int quantity = 2500; // Maior que 1000 para testar batching
        when(productRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        dataGeneratorService.generateRandomProducts(quantity);

        // Then
        // Deve fazer pelo menos 3 chamadas (2 batches de 1000 + 1 batch de 500)
        verify(productRepository, atLeast(3)).saveAll(anyList());
    }

    @Test
    void shouldGenerateProductWithValidFields() {
        // Given
        when(productRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        dataGeneratorService.generateRandomProducts(1);

        // Then
        verify(productRepository).saveAll(argThat(products -> {
            List<Product> productList = (List<Product>) products;
            assertFalse(productList.isEmpty());
            Product product = productList.get(0);
            
            // Verifica se todos os campos obrigatórios estão preenchidos
            assertNotNull(product.getName());
            assertNotNull(product.getDescription());
            assertNotNull(product.getPrice());
            assertNotNull(product.getCreatedAt());
            assertNotNull(product.getUpdatedAt());
            
            // Verifica se o nome do produto contém elementos esperados
            assertTrue(product.getName().contains(" ")); // Deve ter marca + produto + sufixo
            
            // Verifica se a descrição contém texto esperado
            assertTrue(product.getDescription().contains("alta qualidade"));
            assertTrue(product.getDescription().contains("categoria"));
            
            // Verifica se o preço está no range esperado (R$ 10,00 - R$ 9.999,99)
            assertTrue(product.getPrice().compareTo(BigDecimal.valueOf(10.0)) >= 0);
            assertTrue(product.getPrice().compareTo(BigDecimal.valueOf(9999.99)) <= 0);
            
            // Verifica se updatedAt é posterior ao createdAt
            assertTrue(product.getUpdatedAt().isAfter(product.getCreatedAt()) ||
                      product.getUpdatedAt().isEqual(product.getCreatedAt()));
            
            return true;
        }));
    }

    @Test
    void shouldReturnProductCount() {
        // Given
        long expectedCount = 150L;
        when(productRepository.count()).thenReturn(expectedCount);

        // When
        long actualCount = dataGeneratorService.getProductCount();

        // Then
        assertEquals(expectedCount, actualCount);
        verify(productRepository).count();
    }

    @Test
    void shouldClearAllProducts() {
        // Given
        long initialCount = 500L;
        when(productRepository.count()).thenReturn(initialCount);

        // When
        dataGeneratorService.clearAllProducts();

        // Then
        verify(productRepository).count();
        verify(productRepository).deleteAll();
    }

    @Test
    void shouldGenerateProductsWithVariedNames() {
        // Given
        when(productRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        dataGeneratorService.generateRandomProducts(50);

        // Then
        verify(productRepository, atLeastOnce()).saveAll(argThat(products -> {
            // Verifica se os produtos têm nomes diferentes (randomização funcionando)
            List<Product> productList = (List<Product>) products;
            List<String> names = productList.stream()
                    .map(Product::getName)
                    .toList();
            
            // Pelo menos alguns nomes devem ser diferentes
            long uniqueNames = names.stream().distinct().count();
            assertTrue(uniqueNames > 1, "Deve gerar nomes variados");
            
            return true;
        }));
    }

    @Test
    void shouldGenerateProductsWithVariedPrices() {
        // Given
        when(productRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        dataGeneratorService.generateRandomProducts(50);

        // Then
        verify(productRepository, atLeastOnce()).saveAll(argThat(products -> {
            // Verifica se os produtos têm preços diferentes (randomização funcionando)
            List<Product> productList = (List<Product>) products;
            List<BigDecimal> prices = productList.stream()
                    .map(Product::getPrice)
                    .toList();
            
            // Pelo menos alguns preços devem ser diferentes
            long uniquePrices = prices.stream().distinct().count();
            assertTrue(uniquePrices > 1, "Deve gerar preços variados");
            
            return true;
        }));
    }

    @Test
    void shouldGenerateExactQuantityWhenNotMultipleOf1000() {
        // Given
        int quantity = 1234; // Não é múltiplo de 1000
        when(productRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        dataGeneratorService.generateRandomProducts(quantity);

        // Then
        List<List<Product>> allBatches = captureAllSaveAllCalls();
        int totalProductsGenerated = allBatches.stream()
                .mapToInt(List::size)
                .sum();
        
        assertEquals(quantity, totalProductsGenerated);
    }

    @Test
    void shouldGenerateZeroProductsWhenQuantityIsZero() {
        // Given
        int quantity = 0;

        // When
        dataGeneratorService.generateRandomProducts(quantity);

        // Then
        // Não deve fazer nenhuma chamada para saveAll
        verify(productRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldGenerateValidRandomProducts() {
        // Given
        when(productRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        dataGeneratorService.generateRandomProducts(5);

        // Then
        verify(productRepository).saveAll(anyList());
    }

        @Test
    void shouldHandleZeroQuantityGeneration() {
        // Given
        int quantity = 0;

        // When
        dataGeneratorService.generateRandomProducts(quantity);

        // Then
        // Com quantidade 0, não deve chamar saveAll
        verify(productRepository, never()).saveAll(anyList());
    }

    // Método auxiliar para capturar todas as chamadas de saveAll
    @SuppressWarnings("unchecked")
    private List<List<Product>> captureAllSaveAllCalls() {
        return mockingDetails(productRepository)
                .getInvocations()
                .stream()
                .filter(invocation -> invocation.getMethod().getName().equals("saveAll"))
                .map(invocation -> (List<Product>) invocation.getArgument(0))
                .toList();
    }
} 