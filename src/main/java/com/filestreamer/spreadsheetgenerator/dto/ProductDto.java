package com.filestreamer.spreadsheetgenerator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Schema(description = "Dados de um produto")
public class ProductDto {
    
    @Schema(description = "Identificador único do produto", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "Nome do produto", example = "Smartphone Samsung Galaxy")
    private String name;
    
    @Schema(description = "Descrição detalhada do produto", example = "Smartphone com tela de 6.1 polegadas e câmera de 64MP")
    private String description;
    
    @Schema(description = "Preço do produto", example = "999.99")
    private BigDecimal price;
    
    @Schema(description = "Data e hora de criação do produto")
    private LocalDateTime createdAt;
    
    @Schema(description = "Data e hora da última atualização do produto")
    private LocalDateTime updatedAt;

    // Construtores
    public ProductDto() {}

    public ProductDto(UUID id, String name, String description, BigDecimal price, 
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
