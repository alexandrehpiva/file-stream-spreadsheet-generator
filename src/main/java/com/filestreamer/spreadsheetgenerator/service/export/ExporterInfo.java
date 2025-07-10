package com.filestreamer.spreadsheetgenerator.service.export;

/**
 * Classe para informações do exportador
 */
public class ExporterInfo {
    private final ExporterType type;
    private final String info;
    private final boolean configured;
    
    public ExporterInfo(ExporterType type, String info, boolean configured) {
        this.type = type;
        this.info = info;
        this.configured = configured;
    }
    
    public ExporterType getType() { 
        return type; 
    }
    
    public String getInfo() { 
        return info; 
    }
    
    public boolean isConfigured() { 
        return configured; 
    }
    
    public String getDisplayName() { 
        return type.getDisplayName(); 
    }
    
    public String getDescription() { 
        return type.getDescription(); 
    }
} 