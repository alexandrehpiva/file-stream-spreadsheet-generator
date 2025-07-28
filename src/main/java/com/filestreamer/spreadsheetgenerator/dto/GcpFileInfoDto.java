package com.filestreamer.spreadsheetgenerator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * DTO para informações de arquivos no Google Cloud Storage
 */
@Schema(description = "Informações de arquivo no Google Cloud Storage")
public class GcpFileInfoDto {

    @Schema(description = "Nome do arquivo", example = "products_20241201_143022.csv")
    private String name;

    @Schema(description = "Caminho completo do arquivo no bucket", example = "exports/products_20241201_143022.csv")
    private String fullPath;

    @Schema(description = "Tamanho do arquivo em bytes", example = "1024000")
    private Long size;

    @Schema(description = "Tipo de conteúdo do arquivo", example = "text/csv")
    private String contentType;

    @Schema(description = "Data de criação do arquivo")
    private Instant createdAt;

    @Schema(description = "Data da última modificação do arquivo")
    private Instant updatedAt;

    @Schema(description = "URL pública do arquivo (se disponível)")
    private String publicUrl;

    public GcpFileInfoDto() {
    }

    public GcpFileInfoDto(String name, String fullPath, Long size, String contentType, 
                         Instant createdAt, Instant updatedAt, String publicUrl) {
        this.name = name;
        this.fullPath = fullPath;
        this.size = size;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.publicUrl = publicUrl;
    }

    // Getters e Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }
} 