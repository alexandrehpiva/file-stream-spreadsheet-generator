package com.filestreamer.spreadsheetgenerator.service.export;


/**
 * Configuração genérica para exportação de dados.
 * Desacoplada do formato específico dos dados.
 */
public class ExportConfig {
    
    private final String fileName;
    private final String basePath;
    private final String contentType;
    private final String[] headers;
    private final int batchSize;
    
    private ExportConfig(Builder builder) {
        this.fileName = builder.fileName;
        this.basePath = builder.basePath;
        this.contentType = builder.contentType;
        this.headers = builder.headers;
        this.batchSize = builder.batchSize;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getBasePath() {
        return basePath;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public String[] getHeaders() {
        return headers;
    }
    
    public int getBatchSize() {
        return batchSize;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String fileName;
        private String basePath;
        private String contentType = "text/csv";
        private String[] headers;
        private int batchSize = 1000;
        
        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
        
        public Builder basePath(String basePath) {
            this.basePath = basePath;
            return this;
        }
        
        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }
        
        public Builder headers(String[] headers) {
            this.headers = headers;
            return this;
        }
        
        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }
        
        public ExportConfig build() {
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome do arquivo é obrigatório");
            }
            return new ExportConfig(this);
        }
    }
}
