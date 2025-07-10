package com.filestreamer.spreadsheetgenerator.service.export;

import com.filestreamer.spreadsheetgenerator.model.Product;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;


/**
 * Formatador específico para entidades Product
 */
@Component
public class ProductDataFormatter implements DataFormatter<Product> {
    
    private static final DateTimeFormatter CSV_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public String[] formatToRow(Product product) {
        return new String[]{
            product.getId().toString(),
            product.getName(),
            product.getDescription() != null ? product.getDescription() : "",
            product.getPrice().toString(),
            product.getCreatedAt().format(CSV_DATETIME_FORMATTER),
            product.getUpdatedAt().format(CSV_DATETIME_FORMATTER)
        };
    }
    
    @Override
    public String[] getHeaders() {
        return new String[]{
            "ID", "Nome", "Descrição", "Preço", "Data Criação", "Data Atualização"
        };
    }
} 