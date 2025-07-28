package com.filestreamer.spreadsheetgenerator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * DTO para resposta de URL pré-assinada
 */
@Schema(description = "Resposta com URL pré-assinada para download")
public class PresignedUrlResponseDto {

    @Schema(description = "Caminho completo do arquivo no bucket", example = "exports/products_20241201_143022.csv")
    private String filePath;

    @Schema(description = "URL pré-assinada para download do arquivo")
    private String presignedUrl;

    @Schema(description = "Data de expiração da URL pré-assinada")
    private Instant expiresAt;

    @Schema(description = "Duração da URL em horas", example = "1")
    private Long durationHours;

    @Schema(description = "Indica se a URL foi gerada com sucesso", example = "true")
    private Boolean success;

    @Schema(description = "Mensagem de erro (se houver)")
    private String errorMessage;

    public PresignedUrlResponseDto() {
    }

    public PresignedUrlResponseDto(String filePath, String presignedUrl, Instant expiresAt, 
                                 Long durationHours, Boolean success, String errorMessage) {
        this.filePath = filePath;
        this.presignedUrl = presignedUrl;
        this.expiresAt = expiresAt;
        this.durationHours = durationHours;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    // Getters e Setters
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public void setPresignedUrl(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Long durationHours) {
        this.durationHours = durationHours;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
} 