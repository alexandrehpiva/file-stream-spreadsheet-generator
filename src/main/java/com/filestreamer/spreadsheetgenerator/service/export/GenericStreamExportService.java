package com.filestreamer.spreadsheetgenerator.service.export;

import com.filestreamer.spreadsheetgenerator.model.Product;
import com.filestreamer.spreadsheetgenerator.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Serviço genérico de exportação que coordena formatadores de dados e exportadores
 */
@Service
public class GenericStreamExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericStreamExportService.class);
    
    private final ProductRepository productRepository;
    private final ProductDataFormatter productFormatter;
    private final FileNameGenerator fileNameGenerator;
    private final GenericLocalStreamExporter localExporter;
    private final GenericS3StreamExporter s3Exporter;
    private final GenericGcpStreamExporter gcpExporter;
    
    public GenericStreamExportService(ProductRepository productRepository,
                                    ProductDataFormatter productFormatter,
                                    FileNameGenerator fileNameGenerator,
                                    GenericLocalStreamExporter localExporter,
                                    GenericS3StreamExporter s3Exporter,
                                    GenericGcpStreamExporter gcpExporter) {
        this.productRepository = productRepository;
        this.productFormatter = productFormatter;
        this.fileNameGenerator = fileNameGenerator;
        this.localExporter = localExporter;
        this.s3Exporter = s3Exporter;
        this.gcpExporter = gcpExporter;
    }
    
    /**
     * Exporta todos os produtos usando o exportador especificado
     */
    @Transactional(readOnly = true)
    public ExportResult exportAllProducts(ExporterType exporterType, String basePath) throws IOException {
        logger.info("Iniciando exportação genérica de todos os produtos usando {}", exporterType.getDisplayName());
        
        StreamExporter exporter = getExporter(exporterType);
        validateExporter(exporter);
        
        // Configura exportação
        ExportConfig exportConfig = ExportConfig.builder()
                .fileName(fileNameGenerator.generateCsvFileName("products_export"))
                .basePath(basePath)
                .headers(productFormatter.getHeaders())
                .batchSize(1000)
                .build();
        
        try (Stream<Product> productStream = productRepository.findAllByOrderByCreatedAtStream()) {
            // Converte produtos para dados formatados
            Stream<String[]> dataStream = productFormatter.formatToRows(productStream);
            
            return exporter.exportData(dataStream, exportConfig);
        }
    }
    
    /**
     * Exporta produtos filtrados por preço mínimo usando o exportador especificado
     */
    @Transactional(readOnly = true)
    public ExportResult exportProductsByMinPrice(ExporterType exporterType, BigDecimal minPrice, String basePath) throws IOException {
        logger.info("Iniciando exportação genérica de produtos com preço >= {} usando {}", 
                   minPrice, exporterType.getDisplayName());
        
        StreamExporter exporter = getExporter(exporterType);
        validateExporter(exporter);
        
        // Configura exportação com nome específico para filtro
        ExportConfig exportConfig = ExportConfig.builder()
                .fileName(fileNameGenerator.generateFilteredFileName("products", "price_min_" + minPrice, "csv"))
                .basePath(basePath)
                .headers(productFormatter.getHeaders())
                .batchSize(1000)
                .build();
        
        try (Stream<Product> productStream = productRepository.findByPriceGreaterThanEqualStream(minPrice)) {
            // Converte produtos para dados formatados
            Stream<String[]> dataStream = productFormatter.formatToRows(productStream);
            
            return exporter.exportData(dataStream, exportConfig);
        }
    }
    
    /**
     * Exporta dados genéricos usando configuração customizada
     * 
     * @param dataStream Stream de dados já formatados
     * @param exporterType Tipo do exportador
     * @param exportConfig Configuração de exportação
     * @return Resultado da exportação
     */
    public ExportResult exportGenericData(Stream<String[]> dataStream, ExporterType exporterType, ExportConfig exportConfig) throws IOException {
        logger.info("Iniciando exportação genérica usando {} para arquivo: {}", 
                   exporterType.getDisplayName(), exportConfig.getFileName());
        
        StreamExporter exporter = getExporter(exporterType);
        validateExporter(exporter);
        
        return exporter.exportData(dataStream, exportConfig);
    }
    
    /**
     * Retorna informações sobre todos os exportadores disponíveis
     */
    public Map<ExporterType, ExporterInfo> getExportersInfo() {
        return List.of(localExporter, s3Exporter, gcpExporter)
                .stream()
                .collect(Collectors.toMap(
                    StreamExporter::getType,
                    exporter -> new ExporterInfo(
                        exporter.getType(),
                        exporter.getExporterInfo(),
                        exporter.isConfigured()
                    )
                ));
    }
    
    /**
     * Verifica quais exportadores estão configurados e disponíveis
     */
    public List<ExporterType> getAvailableExporters() {
        return List.of(localExporter, s3Exporter, gcpExporter)
                .stream()
                .filter(StreamExporter::isConfigured)
                .map(StreamExporter::getType)
                .collect(Collectors.toList());
    }
    
    private StreamExporter getExporter(ExporterType type) {
        return switch (type) {
            case LOCAL -> localExporter;
            case AWS_S3 -> s3Exporter;
            case GCP_STORAGE -> gcpExporter;
        };
    }
    
    private void validateExporter(StreamExporter exporter) {
        if (!exporter.isConfigured()) {
            throw new IllegalStateException(
                String.format("Exportador %s não está configurado corretamente", 
                            exporter.getType().getDisplayName())
            );
        }
    }
}
