package com.filestreamer.spreadsheetgenerator.service.export;

import com.filestreamer.spreadsheetgenerator.util.GcpPresignedUrlUtil;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.WriteChannel;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;


/**
 * Exportador genérico para Google Cloud Storage
 */
@Component
public class GenericGcpStreamExporter implements StreamExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericGcpStreamExporter.class);
    
    @Value("${GCP_PROJECT_ID:}")
    private String projectId;
    
    @Value("${GCP_STORAGE_BUCKET_NAME:}")
    private String bucketName;
    
    private Storage storage;
    private final GcpPresignedUrlUtil gcpPresignedUrlUtil;
    
    public GenericGcpStreamExporter(GcpPresignedUrlUtil gcpPresignedUrlUtil) {
        this.gcpPresignedUrlUtil = gcpPresignedUrlUtil;
    }
    
    private Storage getStorage() throws IOException {
        if (storage == null) {
            if (!isConfigured()) {
                throw new IllegalStateException("Google Cloud Storage não está configurado corretamente");
            }
            
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            
            storage = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(credentials)
                    .build()
                    .getService();
        }
        return storage;
    }
    
    @Override
    public ExportResult exportData(Stream<String[]> dataStream, ExportConfig exportConfig) throws IOException {
        if (!isConfigured()) {
            logger.error("Exportador {} não está configurado.", getType().getDisplayName());
            return new ExportResult(getType(), "Exportador não configurado: " + getType().getDisplayName());
        }

        logger.info("Iniciando exportação streaming {} para arquivo: {}", getType().getDisplayName(), exportConfig.getFileName());
        
        long startTime = System.currentTimeMillis();
        AtomicLong totalExported = new AtomicLong(0);
        
        try {
            String objectName = buildObjectPath(exportConfig);
            
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(exportConfig.getContentType())
                    .build();
            
            // Usa WriteChannel para streaming ao GCS
            try (WriteChannel writeChannel = getStorage().writer(blobInfo)) {
                Writer writer = Channels.newWriter(writeChannel, StandardCharsets.UTF_8);
                
                try (CSVWriter csvWriter = new CSVWriter(writer)) {
                    // Escreve cabeçalho se fornecido
                    if (exportConfig.getHeaders() != null) {
                        csvWriter.writeNext(exportConfig.getHeaders());
                    }
                    
                    // Processa dados
                    dataStream.forEach(row -> {
                        try {
                            csvWriter.writeNext(row);
                            
                            long count = totalExported.incrementAndGet();
                            
                            // Flush periódico para garantir streaming
                            if (count % exportConfig.getBatchSize() == 0) {
                                logger.info("Processados {} registros via streaming...", count);
                                
                                try {
                                    csvWriter.flush();
                                    writer.flush();
                                } catch (IOException e) {
                                    logger.error("Erro ao fazer flush durante streaming", e);
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Erro ao processar registro: {}", e.getMessage(), e);
                            throw new RuntimeException("Erro durante streaming", e);
                        }
                    });
                    
                    csvWriter.flush();
                    writer.flush();
                }
            }
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // Obtém informações do arquivo criado
            BlobInfo createdBlob = getStorage().get(blobId);
            long fileSize = createdBlob != null ? createdBlob.getSize() : 0;
            
            logger.info("Exportação streaming {} concluída! {} registros exportados em {}ms para gs://{}/{}", 
                       getType().getDisplayName(), totalExported.get(), executionTime, bucketName, objectName);
            
            String fileUrl = generateFileUrl(objectName);
            
            // Gerar URL pré-assinada
            String presignedUrl = null;
            try {
                if (gcpPresignedUrlUtil.isConfigured()) {
                    presignedUrl = gcpPresignedUrlUtil.generatePresignedDownloadUrl(objectName);
                    logger.info("URL pré-assinada gerada com sucesso para gs://{}/{}", bucketName, objectName);
                } else {
                    logger.warn("GcpPresignedUrlUtil não configurado, URL pré-assinada não será gerada");
                }
            } catch (Exception e) {
                logger.error("Erro ao gerar URL pré-assinada para gs://{}/{}: {}", bucketName, objectName, e.getMessage(), e);
                // Continua sem a URL pré-assinada em caso de erro
            }
            
            return new ExportResult(exportConfig.getFileName(), objectName, fileUrl, presignedUrl,
                                  totalExported.get(), fileSize, executionTime, getType());
            
        } catch (Exception e) {
            logger.error("Erro durante exportação streaming {}: {}", getType().getDisplayName(), e.getMessage(), e);
            return new ExportResult(getType(), "Erro durante exportação streaming: " + e.getMessage());
        }
    }
    
    private String buildObjectPath(ExportConfig exportConfig) {
        if (exportConfig.getBasePath() != null && !exportConfig.getBasePath().trim().isEmpty()) {
            String basePath = exportConfig.getBasePath().trim();
            if (!basePath.endsWith("/")) {
                basePath += "/";
            }
            return basePath + exportConfig.getFileName();
        }
        return exportConfig.getFileName();
    }
    
    private String generateFileUrl(String objectName) {
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);
    }
    
    @Override
    public String getExporterInfo() {
        return String.format("Google Cloud Storage (Streaming) - Projeto: %s, Bucket: %s", 
                           projectId, bucketName);
    }
    
    @Override
    public boolean isConfigured() {
        return projectId != null && !projectId.trim().isEmpty() &&
               bucketName != null && !bucketName.trim().isEmpty();
    }
    
    @Override
    public ExporterType getType() {
        return ExporterType.GCP_STORAGE;
    }
}
