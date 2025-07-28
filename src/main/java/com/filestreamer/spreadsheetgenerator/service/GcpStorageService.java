package com.filestreamer.spreadsheetgenerator.service;

import com.filestreamer.spreadsheetgenerator.dto.GcpFileInfoDto;
import com.filestreamer.spreadsheetgenerator.dto.PresignedUrlResponseDto;
import com.filestreamer.spreadsheetgenerator.util.GcpPresignedUrlUtil;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Serviço para gerenciar operações do Google Cloud Storage
 */
@Service
public class GcpStorageService {

    private static final Logger logger = LoggerFactory.getLogger(GcpStorageService.class);

    @Value("${GCP_PROJECT_ID:}")
    private String projectId;

    @Value("${GCP_STORAGE_BUCKET_NAME:}")
    private String bucketName;

    private final GcpPresignedUrlUtil gcpPresignedUrlUtil;

    public GcpStorageService(GcpPresignedUrlUtil gcpPresignedUrlUtil) {
        this.gcpPresignedUrlUtil = gcpPresignedUrlUtil;
    }

    /**
     * Lista todos os arquivos no bucket GCP
     *
     * @return Lista de informações dos arquivos
     * @throws IllegalStateException se as credenciais não estiverem configuradas
     */
    public List<GcpFileInfoDto> listFiles() {
        if (!isConfigured()) {
            throw new IllegalStateException("Google Cloud Storage não está configurado corretamente");
        }

        try {
            Storage storage = getStorage();
            Iterable<Blob> blobs = storage.list(bucketName).iterateAll();

            return StreamSupport.stream(blobs.spliterator(), false)
                    .map(this::convertBlobToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Erro ao listar arquivos no bucket {}: {}", bucketName, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar arquivos no bucket", e);
        }
    }

    /**
     * Lista arquivos no bucket GCP com filtro por prefixo
     *
     * @param prefix Prefixo para filtrar os arquivos
     * @return Lista de informações dos arquivos
     * @throws IllegalStateException se as credenciais não estiverem configuradas
     */
    public List<GcpFileInfoDto> listFilesByPrefix(String prefix) {
        if (!isConfigured()) {
            throw new IllegalStateException("Google Cloud Storage não está configurado corretamente");
        }

        try {
            Storage storage = getStorage();
            Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll();

            return StreamSupport.stream(blobs.spliterator(), false)
                    .map(this::convertBlobToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Erro ao listar arquivos com prefixo '{}' no bucket {}: {}", prefix, bucketName, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar arquivos no bucket", e);
        }
    }

    /**
     * Gera URL pré-assinada para download de arquivo
     *
     * @param filePath Caminho completo do arquivo no bucket
     * @return Resposta com URL pré-assinada
     */
    public PresignedUrlResponseDto generatePresignedUrl(String filePath) {
        if (!isConfigured()) {
            return new PresignedUrlResponseDto(filePath, null, null, null, false, 
                    "Google Cloud Storage não está configurado corretamente");
        }

        try {
            // Verifica se o arquivo existe
            Storage storage = getStorage();
            BlobId blobId = BlobId.of(bucketName, filePath);
            Blob blob = storage.get(blobId);

            if (blob == null) {
                return new PresignedUrlResponseDto(filePath, null, null, null, false, 
                        "Arquivo não encontrado no bucket");
            }

            // Gera URL pré-assinada
            String presignedUrl = gcpPresignedUrlUtil.generatePresignedDownloadUrl(filePath);
            
            // Calcula data de expiração (1 hora)
            Instant expiresAt = Instant.now().plusSeconds(3600);

            logger.info("URL pré-assinada gerada com sucesso para gs://{}/{}", bucketName, filePath);

            return new PresignedUrlResponseDto(filePath, presignedUrl, expiresAt, 1L, true, null);

        } catch (Exception e) {
            logger.error("Erro ao gerar URL pré-assinada para gs://{}/{}: {}", bucketName, filePath, e.getMessage(), e);
            return new PresignedUrlResponseDto(filePath, null, null, null, false, 
                    "Erro ao gerar URL pré-assinada: " + e.getMessage());
        }
    }

    /**
     * Verifica se o arquivo existe no bucket
     *
     * @param filePath Caminho completo do arquivo no bucket
     * @return true se o arquivo existe, false caso contrário
     */
    public boolean fileExists(String filePath) {
        if (!isConfigured()) {
            return false;
        }

        try {
            Storage storage = getStorage();
            BlobId blobId = BlobId.of(bucketName, filePath);
            Blob blob = storage.get(blobId);
            return blob != null;
        } catch (Exception e) {
            logger.error("Erro ao verificar existência do arquivo gs://{}/{}: {}", bucketName, filePath, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Converte Blob para DTO
     */
    private GcpFileInfoDto convertBlobToDto(Blob blob) {
        // Gera URL pública se possível
        String publicUrl = null;
        try {
            publicUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, blob.getName());
        } catch (Exception e) {
            logger.debug("Não foi possível gerar URL pública para {}", blob.getName());
        }
        
        return new GcpFileInfoDto(
                blob.getName().substring(blob.getName().lastIndexOf('/') + 1), // Nome do arquivo
                blob.getName(), // Caminho completo
                blob.getSize(),
                blob.getContentType(),
                blob.getCreateTime() != null ? Instant.ofEpochMilli(blob.getCreateTime()) : null,
                blob.getUpdateTime() != null ? Instant.ofEpochMilli(blob.getUpdateTime()) : null,
                publicUrl
        );
    }

    /**
     * Obtém o Storage client
     */
    private Storage getStorage() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        
        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }

    /**
     * Verifica se as credenciais estão configuradas
     */
    private boolean isConfigured() {
        return projectId != null && !projectId.trim().isEmpty() &&
               bucketName != null && !bucketName.trim().isEmpty();
    }
} 