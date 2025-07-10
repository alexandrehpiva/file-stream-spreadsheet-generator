package com.filestreamer.spreadsheetgenerator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;


@Schema(description = "Dados para criação de um novo produto")
public class ProductCreateDto {
    
    @Schema(description = "Nome do produto", example = "Smartphone Samsung Galaxy", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Nome é obrigatório")
    private String name;
    
    @Schema(description = "Descrição detalhada do produto", example = "Smartphone com tela de 6.1 polegadas e câmera de 64MP")
    private String description;
    
    @Schema(description = "Preço do produto", example = "999.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que zero")
    private BigDecimal price;

    // Construtores
    public ProductCreateDto() {}

    public ProductCreateDto(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    // Getters e Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
