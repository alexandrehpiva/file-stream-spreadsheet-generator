package com.filestreamer.spreadsheetgenerator.service.export;

import com.filestreamer.spreadsheetgenerator.util.GcpPresignedUrlUtil;
import com.filestreamer.spreadsheetgenerator.util.S3PresignedUrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Exemplo de uso das URLs pré-assinadas para S3 e GCP
 */
@Component
public class PresignedUrlExample {
    
    private static final Logger logger = LoggerFactory.getLogger(PresignedUrlExample.class);
    
    private final GenericStreamExportService exportService;
    private final S3PresignedUrlUtil s3PresignedUrlUtil;
    private final GcpPresignedUrlUtil gcpPresignedUrlUtil;
    
    public PresignedUrlExample(GenericStreamExportService exportService,
                              S3PresignedUrlUtil s3PresignedUrlUtil,
                              GcpPresignedUrlUtil gcpPresignedUrlUtil) {
        this.exportService = exportService;
        this.s3PresignedUrlUtil = s3PresignedUrlUtil;
        this.gcpPresignedUrlUtil = gcpPresignedUrlUtil;
    }
    
    /**
     * Exemplo 1: Exportar produtos para S3 com URL pré-assinada
     */
    public ExportResult exportToS3WithPresignedUrl() throws IOException {
        logger.info("=== Exemplo: Exportação para S3 com URL pré-assinada ===");
        
        // Exporta todos os produtos para S3
        ExportResult result = exportService.exportAllProducts(ExporterType.AWS_S3, "exports/examples");
        
        if (result.isSuccess()) {
            logger.info("✅ Exportação S3 bem-sucedida:");
            logger.info("   📁 Arquivo: {}", result.getFileName());
            logger.info("   🔗 URL pública: {}", result.getFileUrl());
            
            if (result.hasPresignedUrl()) {
                logger.info("   🔐 URL pré-assinada (1h): {}", result.getPresignedUrl());
                logger.info("   💡 Use esta URL para download direto e seguro!");
            } else {
                logger.warn("   ⚠️  URL pré-assinada não foi gerada");
            }
            
            logger.info("   📊 {} registros exportados em {}", result.getTotalRecords(), result.getFormattedExecutionTime());
            logger.info("   💾 Tamanho do arquivo: {}", result.getFormattedFileSize());
        } else {
            logger.error("❌ Falha na exportação S3: {}", result.getErrorMessage());
        }
        
        return result;
    }
    
    /**
     * Exemplo 2: Exportar produtos para GCP com URL pré-assinada
     */
    public ExportResult exportToGcpWithPresignedUrl() throws IOException {
        logger.info("=== Exemplo: Exportação para GCP com URL pré-assinada ===");
        
        // Exporta todos os produtos para GCP
        ExportResult result = exportService.exportAllProducts(ExporterType.GCP_STORAGE, "exports/examples");
        
        if (result.isSuccess()) {
            logger.info("✅ Exportação GCP bem-sucedida:");
            logger.info("   📁 Arquivo: {}", result.getFileName());
            logger.info("   🔗 URL pública: {}", result.getFileUrl());
            
            if (result.hasPresignedUrl()) {
                logger.info("   🔐 URL pré-assinada (1h): {}", result.getPresignedUrl());
                logger.info("   💡 Use esta URL para download direto e seguro!");
            } else {
                logger.warn("   ⚠️  URL pré-assinada não foi gerada");
            }
            
            logger.info("   📊 {} registros exportados em {}", result.getTotalRecords(), result.getFormattedExecutionTime());
            logger.info("   💾 Tamanho do arquivo: {}", result.getFormattedFileSize());
        } else {
            logger.error("❌ Falha na exportação GCP: {}", result.getErrorMessage());
        }
        
        return result;
    }
    
    /**
     * Exemplo 3: Gerar URL pré-assinada para arquivo existente no S3
     */
    public void generateS3PresignedUrlForExistingFile(String s3Key) {
        logger.info("=== Exemplo: Gerar URL pré-assinada para arquivo existente no S3 ===");
        
        try {
            if (s3PresignedUrlUtil.isConfigured()) {
                String presignedUrl = s3PresignedUrlUtil.generatePresignedDownloadUrl(s3Key);
                
                logger.info("✅ URL pré-assinada gerada com sucesso:");
                logger.info("   📁 Arquivo S3: s3://bucket/{}", s3Key);
                logger.info("   🔐 URL pré-assinada (1h): {}", presignedUrl);
                logger.info("   ⏰ Válida por: 1 hora");
                
            } else {
                logger.warn("⚠️  S3 não está configurado. Configuração necessária:");
                logger.warn("   - AWS_S3_BUCKET_NAME");
                logger.warn("   - AWS_ACCESS_KEY_ID");
                logger.warn("   - AWS_SECRET_ACCESS_KEY");
                logger.warn("   - AWS_S3_REGION");
            }
            
        } catch (Exception e) {
            logger.error("❌ Erro ao gerar URL pré-assinada S3: {}", e.getMessage());
        }
    }
    
    /**
     * Exemplo 4: Gerar URL pré-assinada para arquivo existente no GCP
     */
    public void generateGcpPresignedUrlForExistingFile(String objectName) {
        logger.info("=== Exemplo: Gerar URL pré-assinada para arquivo existente no GCP ===");
        
        try {
            if (gcpPresignedUrlUtil.isConfigured()) {
                String presignedUrl = gcpPresignedUrlUtil.generatePresignedDownloadUrl(objectName);
                
                logger.info("✅ URL pré-assinada gerada com sucesso:");
                logger.info("   📁 Objeto GCS: gs://bucket/{}", objectName);
                logger.info("   🔐 URL pré-assinada (1h): {}", presignedUrl);
                logger.info("   ⏰ Válida por: 1 hora");
                
            } else {
                logger.warn("⚠️  GCP não está configurado. Configuração necessária:");
                logger.warn("   - GCP_PROJECT_ID");
                logger.warn("   - GCP_STORAGE_BUCKET_NAME");
                logger.warn("   - Credenciais GCP configuradas (Service Account ou Application Default)");
            }
            
        } catch (Exception e) {
            logger.error("❌ Erro ao gerar URL pré-assinada GCP: {}", e.getMessage());
        }
    }
    
    /**
     * Exemplo 5: Comparar resultados entre diferentes exportadores
     */
    public void compareExportResults() throws IOException {
        logger.info("=== Exemplo: Comparação entre exportadores ===");
        
        // Testar LOCAL (não gera URL pré-assinada)
        ExportResult localResult = exportService.exportAllProducts(ExporterType.LOCAL, "exports/comparison");
        logExportComparison("LOCAL", localResult);
        
        // Testar S3 (gera URL pré-assinada)
        try {
            ExportResult s3Result = exportService.exportAllProducts(ExporterType.AWS_S3, "exports/comparison");
            logExportComparison("AWS S3", s3Result);
        } catch (Exception e) {
            logger.warn("⚠️  S3 não disponível: {}", e.getMessage());
        }
        
        // Testar GCP (gera URL pré-assinada)
        try {
            ExportResult gcpResult = exportService.exportAllProducts(ExporterType.GCP_STORAGE, "exports/comparison");
            logExportComparison("GCP Storage", gcpResult);
        } catch (Exception e) {
            logger.warn("⚠️  GCP não disponível: {}", e.getMessage());
        }
    }
    
    private void logExportComparison(String exporterName, ExportResult result) {
        if (result.isSuccess()) {
            logger.info("📊 Resultado {}: {} registros, {}, URL pré-assinada: {}", 
                       exporterName, 
                       result.getTotalRecords(), 
                       result.getFormattedFileSize(),
                       result.hasPresignedUrl() ? "✅ Sim" : "❌ Não");
        } else {
            logger.error("❌ Falha {}: {}", exporterName, result.getErrorMessage());
        }
    }
} 