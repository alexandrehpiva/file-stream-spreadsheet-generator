package com.filestreamer.spreadsheetgenerator.service.export;

import java.time.LocalDateTime;


/**
 * Classe que representa o resultado de uma exportação CSV
 */
public class ExportResult {
    private final String fileName;
    private final String filePath;
    private final String fileUrl;
    private final String presignedUrl; // Nova URL pré-assinada
    private final long totalRecords;
    private final long fileSizeBytes;
    private final long executionTimeMs;
    private final ExporterType exporterType;
    private final LocalDateTime timestamp;
    private final boolean success;
    private final String errorMessage;
    
    // Construtor para sucesso (com URL pré-assinada)
    public ExportResult(String fileName, String filePath, String fileUrl, String presignedUrl,
                       long totalRecords, long fileSizeBytes, long executionTimeMs, 
                       ExporterType exporterType) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileUrl = fileUrl;
        this.presignedUrl = presignedUrl;
        this.totalRecords = totalRecords;
        this.fileSizeBytes = fileSizeBytes;
        this.executionTimeMs = executionTimeMs;
        this.exporterType = exporterType;
        this.timestamp = LocalDateTime.now();
        this.success = true;
        this.errorMessage = null;
    }
    
    // Construtor para sucesso (sem URL pré-assinada - para compatibilidade)
    public ExportResult(String fileName, String filePath, String fileUrl, 
                       long totalRecords, long fileSizeBytes, long executionTimeMs, 
                       ExporterType exporterType) {
        this(fileName, filePath, fileUrl, null, totalRecords, fileSizeBytes, executionTimeMs, exporterType);
    }
    
    // Construtor para erro
    public ExportResult(ExporterType exporterType, String errorMessage) {
        this.fileName = null;
        this.filePath = null;
        this.fileUrl = null;
        this.presignedUrl = null;
        this.totalRecords = 0;
        this.fileSizeBytes = 0;
        this.executionTimeMs = 0;
        this.exporterType = exporterType;
        this.timestamp = LocalDateTime.now();
        this.success = false;
        this.errorMessage = errorMessage;
    }
    
    // Getters
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public String getFileUrl() { return fileUrl; }
    public String getPresignedUrl() { return presignedUrl; }
    public long getTotalRecords() { return totalRecords; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public ExporterType getExporterType() { return exporterType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    
    /**
     * Verifica se há URL pré-assinada disponível
     */
    public boolean hasPresignedUrl() {
        return presignedUrl != null && !presignedUrl.trim().isEmpty();
    }
    
    public String getFormattedFileSize() {
        if (fileSizeBytes < 1024) return fileSizeBytes + " B";
        if (fileSizeBytes < 1024 * 1024) return String.format("%.1f KB", fileSizeBytes / 1024.0);
        if (fileSizeBytes < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", fileSizeBytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    public String getFormattedExecutionTime() {
        if (executionTimeMs < 1000) return executionTimeMs + "ms";
        if (executionTimeMs < 60000) return String.format("%.1fs", executionTimeMs / 1000.0);
        return String.format("%.1fmin", executionTimeMs / 60000.0);
    }
}
