package com.filestreamer.spreadsheetgenerator.service.export;

import com.filestreamer.spreadsheetgenerator.util.S3PresignedUrlUtil;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;


/**
 * Exportador para Amazon S3
 */
@Component
public class GenericS3StreamExporter implements StreamExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericS3StreamExporter.class);
    
    @Value("${AWS_S3_BUCKET_NAME:}")
    private String bucketName;
    
    @Value("${AWS_S3_REGION:us-east-1}")
    private String region;
    
    @Value("${AWS_ACCESS_KEY_ID:}")
    private String accessKeyId;
    
    @Value("${AWS_SECRET_ACCESS_KEY:}")
    private String secretAccessKey;
    
    private S3Client s3Client;
    private final S3PresignedUrlUtil s3PresignedUrlUtil;
    
    public GenericS3StreamExporter(S3PresignedUrlUtil s3PresignedUrlUtil) {
        this.s3PresignedUrlUtil = s3PresignedUrlUtil;
    }
    
    private S3Client getS3Client() {
        if (s3Client == null) {
            if (!isConfigured()) {
                throw new IllegalStateException("AWS S3 não está configurado corretamente");
            }
            
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            
            s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build();
        }
        return s3Client;
    }
    
    @Override
    public ExportResult exportData(Stream<String[]> dataStream, ExportConfig exportConfig) throws IOException {
        if (!isConfigured()) {
            logger.error("Exportador {} não configurado.", getType().getDisplayName());
            return new ExportResult(getType(), "Exportador não configurado: " + getType().getDisplayName());
        }

        logger.info("Iniciando exportação streaming {} para arquivo: {}", getType().getDisplayName(), exportConfig.getFileName());
        
        long startTime = System.currentTimeMillis();
        AtomicLong totalExported = new AtomicLong(0);
        
        try {
            String s3Key = buildS3Key(exportConfig);
            
            // Usa ByteArrayOutputStream com buffer
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                 OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                 CSVWriter csvWriter = new CSVWriter(writer)) {
                
                // Escrever cabeçalho se fornecido
                if (exportConfig.getHeaders() != null) {
                    csvWriter.writeNext(exportConfig.getHeaders());
                }
                
                // Processa os dados
                dataStream.forEach(row -> {
                    try {
                        csvWriter.writeNext(row);
                        
                        long count = totalExported.incrementAndGet();
                        
                        // Log progresso a cada lote
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
                
                // Upload usando o conteúdo do buffer
                byte[] csvBytes = outputStream.toByteArray();
                
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType(exportConfig.getContentType())
                        .contentLength((long) csvBytes.length)
                        .build();
                
                getS3Client().putObject(putObjectRequest, RequestBody.fromBytes(csvBytes));
                
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                long fileSize = csvBytes.length;
                
                logger.info("Exportação streaming {} concluída! {} registros exportados em {}ms para s3://{}/{}", 
                           getType().getDisplayName(), totalExported.get(), executionTime, bucketName, s3Key);
                
                String fileUrl = generateFileUrl(s3Key);
                
                // Gerar URL pré-assinada
                String presignedUrl = null;
                try {
                    if (s3PresignedUrlUtil.isConfigured()) {
                        presignedUrl = s3PresignedUrlUtil.generatePresignedDownloadUrl(s3Key);
                        logger.info("URL pré-assinada gerada com sucesso para s3://{}/{}", bucketName, s3Key);
                    } else {
                        logger.warn("S3PresignedUrlUtil não configurado, URL pré-assinada não será gerada");
                    }
                } catch (Exception e) {
                    logger.error("Erro ao gerar URL pré-assinada para s3://{}/{}: {}", bucketName, s3Key, e.getMessage(), e);
                    // Continua sem a URL pré-assinada em caso de erro
                }
                
                return new ExportResult(exportConfig.getFileName(), s3Key, fileUrl, presignedUrl, 
                                      totalExported.get(), fileSize, executionTime, getType());
            }
            
        } catch (Exception e) {
            logger.error("Erro durante exportação streaming {}: {}", getType().getDisplayName(), e.getMessage(), e);
            return new ExportResult(getType(), "Erro durante exportação streaming: " + e.getMessage());
        }
    }
    
    private String buildS3Key(ExportConfig exportConfig) {
        if (exportConfig.getBasePath() != null && !exportConfig.getBasePath().trim().isEmpty()) {
            String basePath = exportConfig.getBasePath().trim();
            if (!basePath.endsWith("/")) {
                basePath += "/";
            }
            return basePath + exportConfig.getFileName();
        }
        return exportConfig.getFileName();
    }
    
    private String generateFileUrl(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
    }
    
    @Override
    public String getExporterInfo() {
        return String.format("Amazon S3 (Streaming) - Bucket: %s, Região: %s", 
                           bucketName, region);
    }
    
    @Override
    public boolean isConfigured() {
        return bucketName != null && !bucketName.trim().isEmpty() &&
               accessKeyId != null && !accessKeyId.trim().isEmpty() &&
               secretAccessKey != null && !secretAccessKey.trim().isEmpty() &&
               region != null && !region.trim().isEmpty();
    }
    
    @Override
    public ExporterType getType() {
        return ExporterType.AWS_S3;
    }
}
