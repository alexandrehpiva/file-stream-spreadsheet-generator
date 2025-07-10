package com.filestreamer.spreadsheetgenerator.service;

import com.filestreamer.spreadsheetgenerator.dto.ProductCreateDto;
import com.filestreamer.spreadsheetgenerator.dto.ProductDto;
import com.filestreamer.spreadsheetgenerator.dto.ProductUpdateDto;
import com.filestreamer.spreadsheetgenerator.exception.ProductNotFoundException;
import com.filestreamer.spreadsheetgenerator.model.Product;
import com.filestreamer.spreadsheetgenerator.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Serviço para operações de negócio relacionadas a produtos.
 */
@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Cria um novo produto
     */
    @Transactional
    public ProductDto createProduct(ProductCreateDto createDto) {
        logger.info("Criando novo produto: {}", createDto.getName());
        
        // Validar se já existe produto com mesmo nome
        if (productRepository.existsByNameIgnoreCase(createDto.getName())) {
            throw new IllegalArgumentException("Já existe um produto com o nome: " + createDto.getName());
        }

        Product product = new Product(
            createDto.getName().trim(),
            createDto.getDescription() != null ? createDto.getDescription().trim() : null,
            createDto.getPrice()
        );

        Product savedProduct = productRepository.save(product);
        logger.info("Produto criado com sucesso, ID: {}", savedProduct.getId());
        
        return convertToDto(savedProduct);
    }

    /**
     * Busca produto por ID
     */
    @Transactional(readOnly = true)
    public ProductDto findById(UUID id) {
        logger.debug("Buscando produto por ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado com ID: " + id));
            
        return convertToDto(product);
    }

    /**
     * Lista todos os produtos com paginação
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(Pageable pageable) {
        logger.debug("Listando produtos com paginação: {}", pageable);
        
        Page<Product> products = productRepository.findAll(pageable);
        List<ProductDto> productsDto = products.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
            
        return new PageImpl<>(productsDto, pageable, products.getTotalElements());
    }

    /**
     * Busca produtos por nome
     */
    @Transactional(readOnly = true)
    public List<ProductDto> findByName(String name) {
        logger.debug("Buscando produtos por nome: {}", name);
        
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name.trim());
        return products.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Busca produtos por faixa de preço
     */
    @Transactional(readOnly = true)
    public List<ProductDto> findByPriceRange(BigDecimal priceMin, BigDecimal priceMax) {
        logger.debug("Buscando produtos por faixa de preço: {} - {}", priceMin, priceMax);
        
        if (priceMin.compareTo(priceMax) > 0) {
            throw new IllegalArgumentException("Preço mínimo não pode ser maior que preço máximo");
        }
        
        List<Product> products = productRepository.findByPriceRange(priceMin, priceMax);
        return products.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Atualiza um produto existente
     */
    @Transactional
    public ProductDto updateProduct(UUID id, ProductUpdateDto updateDto) {
        logger.info("Atualizando produto ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado com ID: " + id));

        // Verifica se mudou o nome e se já existe outro produto com esse nome
        if (!product.getName().equalsIgnoreCase(updateDto.getName()) &&
            productRepository.existsByNameIgnoreCase(updateDto.getName())) {
            throw new IllegalArgumentException("Já existe um produto com o nome: " + updateDto.getName());
        }

        product.setName(updateDto.getName().trim());
        product.setDescription(updateDto.getDescription() != null ? updateDto.getDescription().trim() : null);
        product.setPrice(updateDto.getPrice());

        Product updatedProduct = productRepository.save(product);
        logger.info("Produto atualizado com sucesso, ID: {}", updatedProduct.getId());
        
        return convertToDto(updatedProduct);
    }

    /**
     * Remove um produto
     */
    @Transactional
    public void deleteProduct(UUID id) {
        logger.info("Removendo produto ID: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Produto não encontrado com ID: " + id);
        }

        productRepository.deleteById(id);
        logger.info("Produto removido com sucesso, ID: {}", id);
    }

    /**
     * Conta total de produtos
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return productRepository.count();
    }

    /**
     * Converte Product para ProductDto
     */
    private ProductDto convertToDto(Product product) {
        return new ProductDto(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}
