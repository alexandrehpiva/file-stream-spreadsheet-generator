package com.filestreamer.spreadsheetgenerator.service.export;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * Serviço responsável pela geração de nomes de arquivo
 */
@Component
public class FileNameGenerator {
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Gera nome de arquivo com timestamp
     * 
     * @param prefix Prefixo do nome do arquivo
     * @param extension Extensão do arquivo (sem o ponto)
     * @return Nome do arquivo gerado
     */
    public String generateFileName(String prefix, String extension) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        return String.format("%s_%s.%s", prefix, timestamp, extension);
    }
    
    /**
     * Gera nome de arquivo CSV com timestamp
     * 
     * @param prefix Prefixo do nome do arquivo
     * @return Nome do arquivo CSV gerado
     */
    public String generateCsvFileName(String prefix) {
        return generateFileName(prefix, "csv");
    }
    
    /**
     * Gera nome de arquivo para exportação filtrada
     * 
     * @param prefix Prefixo base
     * @param filterDescription Descrição do filtro aplicado
     * @param extension Extensão do arquivo
     * @return Nome do arquivo gerado
     */
    public String generateFilteredFileName(String prefix, String filterDescription, String extension) {
        String cleanFilter = filterDescription.replaceAll("[^a-zA-Z0-9_-]", "_");
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        return String.format("%s_%s_%s.%s", prefix, cleanFilter, timestamp, extension);
    }
}
