package com.filestreamer.spreadsheetgenerator.controller;

import com.filestreamer.spreadsheetgenerator.service.export.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v2/export")
@Tag(name = "Generic Export", description = "API para exportação de dados")
public class GenericExportController {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericExportController.class);
    
    private final GenericStreamExportService exportService;
    
    public GenericExportController(GenericStreamExportService exportService) {
        this.exportService = exportService;
    }
    
    /**
     * Endpoint para exportar todos os produtos
     */
    @GetMapping("/products/all")
    @Operation(
        summary = "Exporta todos os produtos",
        description = "Exporta todos os produtos usando streaming"
    )
    @ApiResponse(responseCode = "200", description = "Exportação realizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<ExportResult> exportAllProducts(
            @Parameter(description = "Tipo do exportador (LOCAL, AWS_S3, GCP_STORAGE)")
            @RequestParam ExporterType exporterType,
            @Parameter(description = "Caminho base para salvar o arquivo (opcional)")
            @RequestParam(required = false) String basePath) {
        
        try {
            logger.info("Iniciando exportação genérica de todos os produtos via {}", exporterType.getDisplayName());
            
            ExportResult result = exportService.exportAllProducts(exporterType, basePath);
            
            if (result.isSuccess()) {
                logger.info("Exportação genérica concluída com sucesso: {} registros", result.getTotalRecords());
                return ResponseEntity.ok(result);
            } else {
                logger.error("Falha na exportação genérica: {}", result.getErrorMessage());
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (IllegalStateException e) {
            logger.error("Exportador não configurado: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ExportResult(exporterType, "Exportador não configurado: " + e.getMessage()));
        } catch (IOException e) {
            logger.error("Erro de I/O durante exportação genérica", e);
            return ResponseEntity.internalServerError()
                    .body(new ExportResult(exporterType, "Erro de I/O: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado durante exportação genérica", e);
            return ResponseEntity.internalServerError()
                    .body(new ExportResult(exporterType, "Erro inesperado: " + e.getMessage()));
        }
    }
    
    /**
     * Endpoint para exportar produtos filtrados por preço
     */
    @GetMapping("/products/filtered")
    @Operation(
        summary = "Exporta produtos filtrados por preço",
        description = "Exporta produtos com preço maior ou igual ao valor especificado usando streaming"
    )
    @ApiResponse(responseCode = "200", description = "Exportação realizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    public ResponseEntity<ExportResult> exportFilteredProducts(
            @Parameter(description = "Tipo do exportador (LOCAL, AWS_S3, GCP_STORAGE)")
            @RequestParam ExporterType exporterType,
            @Parameter(description = "Preço mínimo para filtro")
            @RequestParam BigDecimal minPrice,
            @Parameter(description = "Caminho base para salvar o arquivo (opcional)")
            @RequestParam(required = false) String basePath) {
        
        try {
            logger.info("Iniciando exportação genérica filtrada (preço >= {}) via {}", 
                       minPrice, exporterType.getDisplayName());
            
            ExportResult result = exportService.exportProductsByMinPrice(exporterType, minPrice, basePath);
            
            if (result.isSuccess()) {
                logger.info("Exportação genérica filtrada concluída: {} registros", result.getTotalRecords());
                return ResponseEntity.ok(result);
            } else {
                logger.error("Falha na exportação genérica filtrada: {}", result.getErrorMessage());
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (IllegalStateException e) {
            logger.error("Exportador não configurado: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ExportResult(exporterType, "Exportador não configurado: " + e.getMessage()));
        } catch (IOException e) {
            logger.error("Erro de I/O durante exportação genérica filtrada", e);
            return ResponseEntity.internalServerError()
                    .body(new ExportResult(exporterType, "Erro de I/O: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado durante exportação genérica filtrada", e);
            return ResponseEntity.internalServerError()
                    .body(new ExportResult(exporterType, "Erro inesperado: " + e.getMessage()));
        }
    }
    
    /**
     * Endpoint para obter informações sobre os exportadores
     */
    @GetMapping("/exporters/info")
    @Operation(
        summary = "Informações dos exportadores",
        description = "Retorna informações sobre todos os exportadores disponíveis e suas configurações"
    )
    @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso")
    public ResponseEntity<Map<ExporterType, ExporterInfo>> getExportersInfo() {
        try {
            Map<ExporterType, ExporterInfo> exportersInfo = exportService.getExportersInfo();
            return ResponseEntity.ok(exportersInfo);
        } catch (Exception e) {
            logger.error("Erro ao obter informações dos exportadores", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint para obter os exportadores disponíveis
     */
    @GetMapping("/exporters/available")
    @Operation(
        summary = "Exportadores disponíveis",
        description = "Retorna lista dos exportadores que estão configurados e disponíveis para uso"
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<ExporterType>> getAvailableExporters() {
        try {
            List<ExporterType> availableExporters = exportService.getAvailableExporters();
            return ResponseEntity.ok(availableExporters);
        } catch (Exception e) {
            logger.error("Erro ao obter exportadores disponíveis", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
