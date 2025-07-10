package com.filestreamer.spreadsheetgenerator.controller;

import com.filestreamer.spreadsheetgenerator.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
@Tag(name = "Health Check", description = "API para monitoramento da saúde da aplicação")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    private final ProductService productService;
    private final DataSource dataSource;
    private final BuildProperties buildProperties;

    public HealthController(ProductService productService, 
                          DataSource dataSource,
                          @Autowired(required = false) BuildProperties buildProperties) {
        this.productService = productService;
        this.dataSource = dataSource;
        this.buildProperties = buildProperties;
    }

    /**
     * Endpoint para health check completo
     */
    @Operation(summary = "Health check completo", description = "Verifica o status de saúde da aplicação incluindo banco de dados, serviços e JVM")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status de saúde retornado com sucesso")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.debug("Health check requisitado");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("application", "Spreadsheet Generator");
        
        // Informações da aplicação
        if (buildProperties != null) {
            health.put("version", buildProperties.getVersion());
            health.put("buildTime", buildProperties.getTime());
        }
        
        // Verificar componentes
        Map<String, Object> components = new HashMap<>();
        
        // Database Health
        components.put("database", checkDatabaseHealth());
        
        // Service Health  
        components.put("productService", checkProductServiceHealth());
        
        // JVM Health
        components.put("jvm", getJvmHealth());
        
        health.put("components", components);
        
        // Status geral baseado nos componentes
        boolean allHealthy = components.values().stream()
            .allMatch(component -> {
                if (component instanceof Map) {
                    return "UP".equals(((Map<?, ?>) component).get("status"));
                }
                return false;
            });
            
        health.put("status", allHealthy ? "UP" : "DOWN");
        
        return ResponseEntity.ok(health);
    }

    /**
     * Endpoint para health check simplificado
     */
    @Operation(summary = "Health check simples", description = "Verifica rapidamente se a aplicação está funcionando")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status simples retornado com sucesso")
    })
    @GetMapping("/simple")
    public ResponseEntity<Map<String, String>> simpleHealthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para informações detalhadas da aplicação
     */
    @Operation(summary = "Informações da aplicação", description = "Retorna informações detalhadas sobre a aplicação, versão, build e estatísticas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informações da aplicação retornadas com sucesso")
    })
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> applicationInfo() {
        logger.debug("Informações da aplicação requisitadas");
        
        Map<String, Object> info = new HashMap<>();
        
        // Informações básicas
        info.put("application", "Spreadsheet Generator");
        info.put("description", "Sistema para processamento de planilhas com streaming CSV");
        
        if (buildProperties != null) {
            info.put("version", buildProperties.getVersion());
            info.put("buildTime", buildProperties.getTime());
            info.put("group", buildProperties.getGroup());
            info.put("artifact", buildProperties.getArtifact());
        }
        
        // Estatísticas do sistema
        Map<String, Object> stats = new HashMap<>();
        try {
            stats.put("totalProducts", productService.countAll());
        } catch (Exception e) {
            stats.put("totalProducts", "ERRO: " + e.getMessage());
        }
        
        info.put("statistics", stats);
        info.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(info);
    }

    /**
     * Endpoint para verificar saúde do banco de dados
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5); // timeout de 5 segundos
            
            dbHealth.put("status", isValid ? "UP" : "DOWN");
            dbHealth.put("database", connection.getMetaData().getDatabaseProductName());
            dbHealth.put("driver", connection.getMetaData().getDriverName());
            dbHealth.put("url", connection.getMetaData().getURL());
            
        } catch (SQLException e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
            logger.warn("Erro ao verificar saúde do banco de dados", e);
        }
        
        return dbHealth;
    }

    /**
     * Endpoint para verificar saúde do serviço de produtos
     */
    private Map<String, Object> checkProductServiceHealth() {
        Map<String, Object> serviceHealth = new HashMap<>();
        
        try {
            long count = productService.countAll();
            serviceHealth.put("status", "UP");
            serviceHealth.put("totalProducts", count);
            
        } catch (Exception e) {
            serviceHealth.put("status", "DOWN");
            serviceHealth.put("error", e.getMessage());
            logger.warn("Erro ao verificar saúde do serviço de produtos", e);
        }
        
        return serviceHealth;
    }

    /**
     * Endpoint para informações da JVM
     */
    private Map<String, Object> getJvmHealth() {
        Map<String, Object> jvmInfo = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        
        jvmInfo.put("status", "UP");
        jvmInfo.put("javaVersion", System.getProperty("java.version"));
        jvmInfo.put("javaVendor", System.getProperty("java.vendor"));
        
        // Memória
        Map<String, Object> memory = new HashMap<>();
        memory.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
        memory.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
        memory.put("maxMemoryMB", runtime.maxMemory() / (1024 * 1024));
        memory.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        
        jvmInfo.put("memory", memory);
        jvmInfo.put("availableProcessors", runtime.availableProcessors());
        
        return jvmInfo;
    }
}
