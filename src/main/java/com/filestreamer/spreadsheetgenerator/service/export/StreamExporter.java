package com.filestreamer.spreadsheetgenerator.service.export;

import java.io.IOException;
import java.util.stream.Stream;


/**
 * Interface genérica para exportação de dados em streaming
 */
public interface StreamExporter {
    
    /**
     * Exporta dados usando streaming
     * 
     * @param dataStream Stream de dados já formatados como String[]
     * @param exportConfig Configuração de exportação (nome arquivo, caminho, etc.)
     * @return Resultado da exportação
     * @throws IOException em caso de erro na exportação
     */
    ExportResult exportData(Stream<String[]> dataStream, ExportConfig exportConfig) throws IOException;
    
    /**
     * Retorna informações sobre a configuração do exportador
     * 
     * @return String com informações de configuração
     */
    String getExporterInfo();
    
    /**
     * Verifica se o exportador está configurado corretamente
     * 
     * @return true se estiver configurado, false caso contrário
     */
    boolean isConfigured();
    
    /**
     * Retorna o tipo do exportador
     * 
     * @return Tipo do exportador (LOCAL, AWS_S3, GCP_STORAGE)
     */
    ExporterType getType();
}
