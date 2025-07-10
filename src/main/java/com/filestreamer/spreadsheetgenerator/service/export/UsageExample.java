package com.filestreamer.spreadsheetgenerator.service.export;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Stream;


/**
 * Exemplo de uso da arquitetura desacoplada de exportação
 */
@Component
public class UsageExample {
    
    private final GenericLocalStreamExporter localExporter;
    private final GenericS3StreamExporter s3Exporter;
    private final GenericGcpStreamExporter gcpExporter;
    private final FileNameGenerator fileNameGenerator;
    
    public UsageExample(GenericLocalStreamExporter localExporter,
                       GenericS3StreamExporter s3Exporter,
                       GenericGcpStreamExporter gcpExporter,
                       FileNameGenerator fileNameGenerator) {
        this.localExporter = localExporter;
        this.s3Exporter = s3Exporter;
        this.gcpExporter = gcpExporter;
        this.fileNameGenerator = fileNameGenerator;
    }
    
    /**
     * Exemplo 1: Exportar dados de vendas customizados
     */
    public ExportResult exportSalesData() throws IOException {
        // Dados de vendas fictícios
        Stream<String[]> salesData = Stream.of(
            new String[]{"2024-01-01", "Produto A", "100.00", "10", "1000.00"},
            new String[]{"2024-01-02", "Produto B", "50.00", "20", "1000.00"},
            new String[]{"2024-01-03", "Produto C", "25.00", "40", "1000.00"}
        );
        
        // Configuração personalizada
        ExportConfig config = ExportConfig.builder()
                .fileName(fileNameGenerator.generateCsvFileName("vendas_relatorio"))
                .basePath("relatorios/vendas")
                .headers(new String[]{"Data", "Produto", "Preço Unit.", "Quantidade", "Total"})
                .batchSize(500)
                .build();
        
        // Exporta para local (pode ser facilmente mudado para S3 ou GCP)
        return localExporter.exportData(salesData, config);
    }
    
    /**
     * Exemplo 2: Exportar relatório de usuários
     */
    public ExportResult exportUsersReport() throws IOException {
        // Dados de usuários fictícios
        Stream<String[]> userData = Stream.of(
            new String[]{"1", "João Silva", "joao@email.com", "Ativo", "2024-01-01"},
            new String[]{"2", "Maria Santos", "maria@email.com", "Ativo", "2024-01-02"},
            new String[]{"3", "Pedro Oliveira", "pedro@email.com", "Inativo", "2024-01-03"}
        );
        
        // Configuração para relatório de usuários
        ExportConfig config = ExportConfig.builder()
                .fileName(fileNameGenerator.generateCsvFileName("usuarios_relatorio"))
                .basePath("relatorios/usuarios")
                .headers(new String[]{"ID", "Nome", "Email", "Status", "Data Cadastro"})
                .batchSize(1000)
                .contentType("text/csv; charset=utf-8")
                .build();
        
        // Exporta para GCP (pode ser facilmente mudado)
        return gcpExporter.exportData(userData, config);
    }
    
    /**
     * Exemplo 3: Exportar dados financeiros para S3
     */
    public ExportResult exportFinancialData() throws IOException {
        // Dados financeiros fictícios
        Stream<String[]> financialData = Stream.of(
            new String[]{"2024-01", "Receita", "50000.00", "BRL"},
            new String[]{"2024-01", "Despesa", "30000.00", "BRL"},
            new String[]{"2024-01", "Lucro", "20000.00", "BRL"}
        );
        
        // Configuração para dados financeiros
        ExportConfig config = ExportConfig.builder()
                .fileName(fileNameGenerator.generateCsvFileName("financeiro_mensal"))
                .basePath("financeiro/2024")
                .headers(new String[]{"Período", "Categoria", "Valor", "Moeda"})
                .batchSize(2000)
                .build();
        
        // Exporta para S3
        return s3Exporter.exportData(financialData, config);
    }
    
    /**
     * Exemplo 4: Exportar dados genéricos com configuração dinâmica
     */
    public ExportResult exportGenericData(Stream<String[]> data, 
                                        String[] headers, 
                                        String filePrefix,
                                        String folder,
                                        ExporterType exporterType) throws IOException {
        
        // Configuração dinâmica
        ExportConfig config = ExportConfig.builder()
                .fileName(fileNameGenerator.generateCsvFileName(filePrefix))
                .basePath(folder)
                .headers(headers)
                .batchSize(1000)
                .build();
        
        // Escolhe exportador dinamicamente
        StreamExporter exporter = switch (exporterType) {
            case LOCAL -> localExporter;
            case AWS_S3 -> s3Exporter;
            case GCP_STORAGE -> gcpExporter;
        };
        
        return exporter.exportData(data, config);
    }
}
