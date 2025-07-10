package com.filestreamer.spreadsheetgenerator.controller;

import com.filestreamer.spreadsheetgenerator.dto.ProductCreateDto;
import com.filestreamer.spreadsheetgenerator.dto.ProductDto;
import com.filestreamer.spreadsheetgenerator.dto.ProductUpdateDto;
import com.filestreamer.spreadsheetgenerator.exception.ProductNotFoundException;
import com.filestreamer.spreadsheetgenerator.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/products")
@Validated
@CrossOrigin(origins = "*")
@Tag(name = "Products", description = "API para gerenciamento de produtos")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Endpoint para criar um novo produto
     */
    @Operation(summary = "Criar produto", description = "Cria um novo produto no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductCreateDto createDto) {
        logger.info("Recebida requisição para criar produto: {}", createDto.getName());
        
        try {
            ProductDto product = productService.createProduct(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao criar produto: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint para buscar produto por ID
     */
    @Operation(summary = "Buscar produto por ID", description = "Retorna um produto específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> findById(
            @Parameter(description = "ID do produto", required = true) @PathVariable UUID id) {
        logger.debug("Recebida requisição para buscar produto por ID: {}", id);
        
        try {
            ProductDto product = productService.findById(id);
            return ResponseEntity.ok(product);
        } catch (ProductNotFoundException e) {
            logger.warn("Produto não encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint para listar todos os produtos com paginação
     */
    @Operation(summary = "Listar produtos", description = "Lista todos os produtos com paginação e ordenação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> listProducts(
            @Parameter(description = "Número da página (inicia em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direção da ordenação (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.debug("Recebida requisição para listar produtos - page: {}, size: {}", page, size);
        
        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDir);
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<ProductDto> products = productService.findAll(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("products", products.getContent());
            response.put("currentPage", products.getNumber());
            response.put("totalItems", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());
            response.put("hasNext", products.hasNext());
            response.put("hasPrevious", products.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao listar produtos", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para buscar produtos por nome
     */
    @Operation(summary = "Buscar produtos por nome", description = "Busca produtos que contenham o nome especificado (busca parcial, case-insensitive)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos encontrados"),
            @ApiResponse(responseCode = "400", description = "Nome não pode estar vazio")
    })
    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> findByName(
            @Parameter(description = "Nome ou parte do nome do produto", required = true) @RequestParam String name) {
        logger.debug("Recebida requisição para buscar produtos por nome: {}", name);
        
        if (name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ProductDto> products = productService.findByName(name);
        return ResponseEntity.ok(products);
    }

    /**
     * Endpoint para buscar produtos por faixa de preço
     */
    @Operation(summary = "Buscar produtos por faixa de preço", description = "Busca produtos dentro de uma faixa específica de preços")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos na faixa de preço especificada"),
            @ApiResponse(responseCode = "400", description = "Preço mínimo não pode ser maior que preço máximo")
    })
    @GetMapping("/price-range")
    public ResponseEntity<List<ProductDto>> findByPriceRange(
            @Parameter(description = "Preço mínimo", required = true, example = "10.00") @RequestParam BigDecimal priceMin,
            @Parameter(description = "Preço máximo", required = true, example = "100.00") @RequestParam BigDecimal priceMax) {
        
        logger.debug("Recebida requisição para buscar produtos por faixa de preço: {} - {}", priceMin, priceMax);
        
        try {
            List<ProductDto> products = productService.findByPriceRange(priceMin, priceMax);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação na busca por faixa de preço: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint para atualizar produto existente
     */
    @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome já existe")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @Parameter(description = "ID do produto a ser atualizado", required = true) @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateDto updateDto) {
        
        logger.info("Recebida requisição para atualizar produto ID: {}", id);
        
        try {
            ProductDto product = productService.updateProduct(id, updateDto);
            return ResponseEntity.ok(product);
        } catch (ProductNotFoundException e) {
            logger.warn("Produto não encontrado para atualização: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao atualizar produto: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint para remover produto
     */
    @Operation(summary = "Deletar produto", description = "Remove um produto do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID do produto a ser removido", required = true) @PathVariable UUID id) {
        logger.info("Recebida requisição para remover produto ID: {}", id);
        
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (ProductNotFoundException e) {
            logger.warn("Produto não encontrado para remoção: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint para contar total de produtos
     */
    @Operation(summary = "Contar produtos", description = "Retorna o número total de produtos cadastrados no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem de produtos retornada com sucesso")
    })
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countProducts() {
        logger.debug("Recebida requisição para contar produtos");
        
        long total = productService.countAll();
        Map<String, Long> response = new HashMap<>();
        response.put("total", total);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Handler para tratar exceções de validação global
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Erro de validação");
        error.put("message", e.getMessage());
        
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handler para tratar exceções de produto não encontrado
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFoundException(ProductNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Produto não encontrado");
        error.put("message", e.getMessage());
        
        return ResponseEntity.notFound().build();
    }
}
