package com.filestreamer.spreadsheetgenerator.service.export;


/**
 * Enum que define os tipos de exportadores CSV disponíveis
 */
public enum ExporterType {
    LOCAL("Local File System", "Salva arquivos no sistema de arquivos local"),
    AWS_S3("Amazon S3", "Salva arquivos no Amazon S3"),
    GCP_STORAGE("Google Cloud Storage", "Salva arquivos no Google Cloud Storage");
    
    private final String displayName;
    private final String description;
    
    ExporterType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
