package com.filestreamer.spreadsheetgenerator.controller;

import com.filestreamer.spreadsheetgenerator.service.DataGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/data-generator")
@Tag(name = "Data Generator", description = "Geração de dados aleatórios para testes e desenvolvimento")
public class DataGeneratorController {

    @Autowired
    private DataGeneratorService dataGeneratorService;

    @PostMapping("/products/{quantity}")
    @Operation(
        summary = "Gerar produtos aleatórios",
        description = "Gera uma quantidade específica de produtos com dados aleatórios realistas. " +
                "Útil para popular o banco de dados para testes de performance, desenvolvimento e demonstrações. " +
                "Os produtos gerados incluem nomes, descrições, preços, categorias e quantidades variadas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produtos gerados com sucesso"),
        @ApiResponse(responseCode = "400", description = "Quantidade inválida (deve ser entre 1 e 1.000.000)"),
        @ApiResponse(responseCode = "500", description = "Erro interno durante a geração dos dados")
    })
    public ResponseEntity<Map<String, Object>> generateProducts(
            @Parameter(
                description = "Quantidade de produtos a serem gerados (máximo: 1.000.000)",
                example = "100000"
            )
            @PathVariable int quantity) {
        
        if (quantity <= 0 || quantity > 1_000_000) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Quantidade deve estar entre 1 e 1.000.000");
            errorResponse.put("requested_quantity", quantity);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        long startTime = System.currentTimeMillis();
        long initialCount = dataGeneratorService.getProductCount();
        
        dataGeneratorService.generateRandomProducts(quantity);
        
        long endTime = System.currentTimeMillis();
        long finalCount = dataGeneratorService.getProductCount();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Produtos gerados com sucesso");
        response.put("generated_quantity", quantity);
        response.put("execution_time_ms", endTime - startTime);
        response.put("products_per_second", Math.round((double) quantity / (endTime - startTime) * 1000));
        response.put("initial_count", initialCount);
        response.put("final_count", finalCount);
        response.put("total_products_in_database", finalCount);
        
        return ResponseEntity.ok(response);
    }



    @DeleteMapping("/products/clear")
    @Operation(
        summary = "Limpar todos os produtos",
        description = "Remove todos os produtos do banco de dados. " +
                "⚠️ ATENÇÃO: Esta operação é irreversível e remove TODOS os produtos!"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Todos os produtos foram removidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno durante a remoção")
    })
    public ResponseEntity<Map<String, Object>> clearAllProducts() {
        long initialCount = dataGeneratorService.getProductCount();
        
        dataGeneratorService.clearAllProducts();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Todos os produtos foram removidos");
        response.put("removed_count", initialCount);
        response.put("remaining_count", 0);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(
        summary = "Informações do gerador de dados",
        description = "Retorna informações sobre as capacidades e configurações do gerador de dados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso")
    })
    public ResponseEntity<Map<String, Object>> getGeneratorInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Data Generator API");
        response.put("description", "Gerador de dados aleatórios para produtos");
        response.put("max_quantity_per_request", 1_000_000);
        response.put("batch_size", 1_000);
        response.put("features", new String[]{
            "Nomes de produtos realistas",
            "Preços variados (R$ 10,00 - R$ 9.999,99)",
            "Categorias diversificadas",
            "Descrições automáticas",
            "Quantidades em estoque aleatórias",
            "Datas de criação variadas",
            "Processamento em lotes para performance"
        });
        response.put("current_products_count", dataGeneratorService.getProductCount());
        
        return ResponseEntity.ok(response);
    }
} 