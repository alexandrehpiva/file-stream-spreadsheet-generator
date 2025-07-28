package com.filestreamer.spreadsheetgenerator.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Classe utilitária para gerar URLs pré-assinadas do Google Cloud Storage
 */
@Component
public class GcpPresignedUrlUtil {

    private static final Logger logger = LoggerFactory.getLogger(GcpPresignedUrlUtil.class);

    private static final long PRESIGNED_URL_DURATION_HOURS = 1;

    @Value("${GCP_PROJECT_ID:}")
    private String projectId;

    @Value("${GCP_STORAGE_BUCKET_NAME:}")
    private String bucketName;

    @Value("${GOOGLE_APPLICATION_CREDENTIALS:}")
    private String credentialsPath;

    private Storage storage;

    /**
     * Gera uma URL pré-assinada para download de arquivo do GCS
     *
     * @param objectName Nome do objeto no GCS
     * @return URL pré-assinada válida por 1 hora
     * @throws IllegalStateException se as credenciais não estiverem configuradas
     */
    public String generatePresignedDownloadUrl(String objectName) {
        if (!isConfigured()) {
            throw new IllegalStateException("Credenciais GCP não configuradas");
        }

        try {
            Storage storage = getStorage();

            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            URL presignedUrl = storage.signUrl(
                    blobInfo,
                    PRESIGNED_URL_DURATION_HOURS,
                    TimeUnit.HOURS,
                    Storage.SignUrlOption.httpMethod(HttpMethod.GET)
            );

            String presignedUrlString = presignedUrl.toString();

            logger.info("URL pré-assinada gerada para gs://{}/{} com expiração em {} hora(s)",
                    bucketName, objectName, PRESIGNED_URL_DURATION_HOURS);

            return presignedUrlString;

        } catch (Exception e) {
            logger.error("Erro ao gerar URL pré-assinada para gs://{}/{}: {}",
                    bucketName, objectName, e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar URL pré-assinada do GCS", e);
        }
    }

    /**
     * Verifica se as credenciais estão configuradas
     *
     * @return true se configurado, false caso contrário
     */
    public boolean isConfigured() {
        return projectId != null && !projectId.trim().isEmpty() &&
                bucketName != null && !bucketName.trim().isEmpty() &&
                credentialsPath != null && !credentialsPath.trim().isEmpty();
    }

    /**
     * Obtém o Storage client (lazy initialization)
     *
     * @return Storage client configurado
     * @throws IOException se houver erro ao obter credenciais
     */
    private Storage getStorage() throws IOException {
        if (storage == null) {
            if (!isConfigured()) {
                throw new IllegalStateException("Google Cloud Storage não está configurado corretamente");
            }

            try (FileInputStream credentialsStream = new FileInputStream(credentialsPath)) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

                storage = StorageOptions.newBuilder()
                        .setProjectId(projectId)
                        .setCredentials(credentials)
                        .build()
                        .getService();
            }
        }
        return storage;
    }
} 