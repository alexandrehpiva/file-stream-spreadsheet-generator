package com.filestreamer.spreadsheetgenerator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

/**
 * Classe utilitária para gerar URLs pré-assinadas do Amazon S3
 */
@Component
public class S3PresignedUrlUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(S3PresignedUrlUtil.class);
    
    private static final Duration PRESIGNED_URL_DURATION = Duration.ofHours(1);
    
    @Value("${AWS_S3_BUCKET_NAME:}")
    private String bucketName;
    
    @Value("${AWS_S3_REGION:us-east-1}")
    private String region;
    
    @Value("${AWS_ACCESS_KEY_ID:}")
    private String accessKeyId;
    
    @Value("${AWS_SECRET_ACCESS_KEY:}")
    private String secretAccessKey;
    
    private S3Presigner s3Presigner;
    
    /**
     * Gera uma URL pré-assinada para download de arquivo do S3
     *
     * @param s3Key Chave do objeto no S3
     * @return URL pré-assinada válida por 1 hora
     * @throws IllegalStateException se as credenciais não estiverem configuradas
     */
    public String generatePresignedDownloadUrl(String s3Key) {
        if (!isConfigured()) {
            throw new IllegalStateException("Credenciais AWS S3 não configuradas");
        }
        
        try {
            S3Presigner presigner = getS3Presigner();
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(PRESIGNED_URL_DURATION)
                    .getObjectRequest(getObjectRequest)
                    .build();
            
            PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);
            
            String presignedUrl = presignedGetObjectRequest.url().toString();
            
            logger.info("URL pré-assinada gerada para s3://{}/{} com expiração em {} hora(s)", 
                       bucketName, s3Key, PRESIGNED_URL_DURATION.toHours());
            
            return presignedUrl;
            
        } catch (Exception e) {
            logger.error("Erro ao gerar URL pré-assinada para s3://{}/{}: {}", 
                        bucketName, s3Key, e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar URL pré-assinada do S3", e);
        }
    }
    
    /**
     * Verifica se as credenciais estão configuradas
     *
     * @return true se configurado, false caso contrário
     */
    public boolean isConfigured() {
        return bucketName != null && !bucketName.trim().isEmpty() &&
               accessKeyId != null && !accessKeyId.trim().isEmpty() &&
               secretAccessKey != null && !secretAccessKey.trim().isEmpty() &&
               region != null && !region.trim().isEmpty();
    }
    
    /**
     * Obtém o S3Presigner (lazy initialization)
     *
     * @return S3Presigner configurado
     */
    private S3Presigner getS3Presigner() {
        if (s3Presigner == null) {
            if (!isConfigured()) {
                throw new IllegalStateException("AWS S3 não está configurado corretamente");
            }
            
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            
            s3Presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build();
        }
        return s3Presigner;
    }
    
    /**
     * Fecha o S3Presigner quando não precisar mais
     */
    public void close() {
        if (s3Presigner != null) {
            s3Presigner.close();
            s3Presigner = null;
        }
    }
} 