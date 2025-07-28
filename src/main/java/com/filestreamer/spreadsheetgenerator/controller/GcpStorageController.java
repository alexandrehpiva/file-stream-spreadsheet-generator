package com.filestreamer.spreadsheetgenerator.controller;

import com.filestreamer.spreadsheetgenerator.dto.GcpFileInfoDto;
import com.filestreamer.spreadsheetgenerator.dto.PresignedUrlResponseDto;
import com.filestreamer.spreadsheetgenerator.service.GcpStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para operações do Google Cloud Storage
 */
@RestController
@RequestMapping("/api/v2/gcp")
@Tag(name = "Google Cloud Storage", description = "API para operações no Google Cloud Storage")
public class GcpStorageController {

    private static final Logger logger = LoggerFactory.getLogger(GcpStorageController.class);

    private final GcpStorageService gcpStorageService;

    public GcpStorageController(GcpStorageService gcpStorageService) {
        this.gcpStorageService = gcpStorageService;
    }

    /**
     * Lista todos os arquivos no bucket GCP
     */
    @GetMapping("/files")
    @Operation(
        summary = "Lista todos os arquivos no bucket GCP",
        description = "Retorna uma lista com informações de todos os arquivos armazenados no bucket do Google Cloud Storage"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de arquivos retornada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Configuração do GCP inválida"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<GcpFileInfoDto>> listAllFiles() {
        try {
            logger.info("Listando todos os arquivos no bucket GCP");
            
            List<GcpFileInfoDto> files = gcpStorageService.listFiles();
            
            logger.info("Encontrados {} arquivos no bucket GCP", files.size());
            return ResponseEntity.ok(files);
            
        } catch (IllegalStateException e) {
            logger.error("GCP não configurado: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao listar arquivos no bucket GCP", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lista arquivos no bucket GCP com filtro por prefixo
     */
    @GetMapping("/files/filtered")
    @Operation(
        summary = "Lista arquivos no bucket GCP com filtro por prefixo",
        description = "Retorna uma lista com informações dos arquivos que começam com o prefixo especificado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de arquivos retornada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Configuração do GCP inválida"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<GcpFileInfoDto>> listFilesByPrefix(
            @Parameter(description = "Prefixo para filtrar os arquivos", example = "exports/")
            @RequestParam String prefix) {
        
        try {
            logger.info("Listando arquivos no bucket GCP com prefixo: {}", prefix);
            
            List<GcpFileInfoDto> files = gcpStorageService.listFilesByPrefix(prefix);
            
            logger.info("Encontrados {} arquivos no bucket GCP com prefixo '{}'", files.size(), prefix);
            return ResponseEntity.ok(files);
            
        } catch (IllegalStateException e) {
            logger.error("GCP não configurado: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao listar arquivos no bucket GCP com prefixo '{}'", prefix, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Gera URL pré-assinada para download de arquivo
     */
    @PostMapping("/files/presigned-url")
    @Operation(
        summary = "Gera URL pré-assinada para download de arquivo",
        description = "Gera uma URL pré-assinada válida por 1 hora para download do arquivo especificado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "URL pré-assinada gerada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Configuração do GCP inválida ou arquivo não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<PresignedUrlResponseDto> generatePresignedUrl(
            @Parameter(description = "Caminho completo do arquivo no bucket", example = "exports/products_20241201_143022.csv")
            @RequestParam String filePath) {
        
        try {
            logger.info("Gerando URL pré-assinada para arquivo: {}", filePath);
            
            PresignedUrlResponseDto response = gcpStorageService.generatePresignedUrl(filePath);
            
            if (response.getSuccess()) {
                logger.info("URL pré-assinada gerada com sucesso para: {}", filePath);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Falha ao gerar URL pré-assinada para {}: {}", filePath, response.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Erro ao gerar URL pré-assinada para {}", filePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Verifica se um arquivo existe no bucket
     */
    @GetMapping("/files/exists")
    @Operation(
        summary = "Verifica se arquivo existe no bucket",
        description = "Verifica se o arquivo especificado existe no bucket do Google Cloud Storage"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Configuração do GCP inválida"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Boolean> fileExists(
            @Parameter(description = "Caminho completo do arquivo no bucket", example = "exports/products_20241201_143022.csv")
            @RequestParam String filePath) {
        
        try {
            logger.info("Verificando existência do arquivo: {}", filePath);
            
            boolean exists = gcpStorageService.fileExists(filePath);
            
            logger.info("Arquivo {} existe: {}", filePath, exists);
            return ResponseEntity.ok(exists);
            
        } catch (Exception e) {
            logger.error("Erro ao verificar existência do arquivo {}", filePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 