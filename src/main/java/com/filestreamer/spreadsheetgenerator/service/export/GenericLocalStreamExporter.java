package com.filestreamer.spreadsheetgenerator.service.export;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;


@Component
public class GenericLocalStreamExporter implements StreamExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericLocalStreamExporter.class);
    
    @Value("${CSV_EXPORT_PATH:./temp}")
    private String defaultExportPath;
    
    @Override
    public ExportResult exportData(Stream<String[]> dataStream, ExportConfig exportConfig) throws IOException {
        logger.info("Iniciando exportação streaming {} para arquivo: {}", getType().getDisplayName(), exportConfig.getFileName());
        
        long startTime = System.currentTimeMillis();
        AtomicLong totalExported = new AtomicLong(0);
        
        try {
            Path filePath = buildFilePath(exportConfig);
            
            // Cria diretório se não existir
            Path directory = filePath.getParent();
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
                logger.info("Diretório criado: {}", directory);
            }
            
            // Usa FileWriter para streaming direto ao arquivo
            try (FileWriter fileWriter = new FileWriter(filePath.toFile());
                 CSVWriter csvWriter = new CSVWriter(fileWriter)) {
                
                // Escreve cabeçalho se fornecido
                if (exportConfig.getHeaders() != null) {
                    csvWriter.writeNext(exportConfig.getHeaders());
                }
                
                // Processa dados
                dataStream.forEach(row -> {
                    try {
                        csvWriter.writeNext(row);
                        
                        long count = totalExported.incrementAndGet();
                        
                        // Flush periódico para garantir streaming
                        if (count % exportConfig.getBatchSize() == 0) {
                            logger.info("Processados {} registros via streaming...", count);
                            
                            try {
                                csvWriter.flush();
                                fileWriter.flush();
                            } catch (IOException e) {
                                logger.error("Erro ao fazer flush durante streaming", e);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Erro ao processar registro: {}", e.getMessage(), e);
                        throw new RuntimeException("Erro durante streaming", e);
                    }
                });
                
                csvWriter.flush();
                fileWriter.flush();
            }
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            long fileSize = Files.size(filePath);
            
            logger.info("Exportação streaming {} concluída! {} registros exportados em {}ms para arquivo: {}", 
                       getType().getDisplayName(), totalExported.get(), executionTime, filePath);
            
            String fileUrl = filePath.toString();
            
            return new ExportResult(exportConfig.getFileName(), filePath.toString(), fileUrl, totalExported.get(), 
                                  fileSize, executionTime, getType());
            
        } catch (Exception e) {
            logger.error("Erro durante exportação streaming {}: {}", getType().getDisplayName(), e.getMessage(), e);
            return new ExportResult(getType(), "Erro durante exportação streaming: " + e.getMessage());
        }
    }
    
    private Path buildFilePath(ExportConfig exportConfig) {
        Path basePath;
        
        if (exportConfig.getBasePath() != null && !exportConfig.getBasePath().trim().isEmpty()) {
            basePath = Paths.get(exportConfig.getBasePath().trim());
        } else {
            basePath = Paths.get(defaultExportPath);
        }
        
        return basePath.resolve(exportConfig.getFileName());
    }
    
    @Override
    public String getExporterInfo() {
        return String.format("Local File System (Streaming) - Diretório padrão: %s", defaultExportPath);
    }
    
    @Override
    public boolean isConfigured() {
        try {
            Path directory = Paths.get(defaultExportPath);
            return Files.exists(directory) || Files.isWritable(directory.getParent());
        } catch (Exception e) {
            logger.warn("Erro ao verificar configuração do exportador local", e);
            return false;
        }
    }
    
    @Override
    public ExporterType getType() {
        return ExporterType.LOCAL;
    }
}
