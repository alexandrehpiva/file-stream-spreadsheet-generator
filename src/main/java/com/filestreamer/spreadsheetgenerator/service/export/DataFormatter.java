package com.filestreamer.spreadsheetgenerator.service.export;

import java.util.stream.Stream;


/**
 * Interface genérica para formatação de dados para exportação.
 * Permite converter qualquer tipo de objeto em dados formatados para CSV.
 * 
 * @param <T> Tipo da entidade a ser formatada
 */
public interface DataFormatter<T> {
    
    /**
     * Converte um objeto em uma linha CSV (array de strings)
     * 
     * @param entity Entidade a ser convertida
     * @return Array de strings representando uma linha CSV
     */
    String[] formatToRow(T entity);
    
    /**
     * Retorna o cabeçalho das colunas
     * 
     * @return Array de strings com os nomes das colunas
     */
    String[] getHeaders();
    
    /**
     * Converte um stream de entidades em stream de linhas CSV
     * 
     * @param entityStream Stream de entidades
     * @return Stream de arrays de strings (linhas CSV)
     */
    default Stream<String[]> formatToRows(Stream<T> entityStream) {
        return entityStream.map(this::formatToRow);
    }
}
