package com.filestreamer.spreadsheetgenerator.repository;

import com.filestreamer.spreadsheetgenerator.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;


@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Busca produtos por nome
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Busca produtos dentro de uma faixa de preço
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :priceMin AND :priceMax ORDER BY p.price")
    List<Product> findByPriceRange(@Param("priceMin") BigDecimal priceMin, @Param("priceMax") BigDecimal priceMax);

    /**
     * Busca produtos por nome com paginação
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Stream de todos os produtos para processamento em lote (export CSV)
     */
    @Query("SELECT p FROM Product p ORDER BY p.createdAt")
    Stream<Product> findAllByOrderByCreatedAtStream();

    /**
     * Stream de produtos com preço maior ou igual ao valor especificado
     */
    @Query("SELECT p FROM Product p WHERE p.price >= :priceMin ORDER BY p.price")
    Stream<Product> findByPriceGreaterThanEqualStream(@Param("priceMin") BigDecimal priceMin);

    /**
     * Conta produtos dentro de uma faixa de preço
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.price BETWEEN :priceMin AND :priceMax")
    Long countByPriceRange(@Param("priceMin") BigDecimal priceMin, @Param("priceMax") BigDecimal priceMax);

    /**
     * Busca os produtos mais caros
     */
    @Query("SELECT p FROM Product p ORDER BY p.price DESC")
    List<Product> findTopByOrderByPriceDesc(Pageable pageable);

    /**
     * Verifica se existe produto com nome específico (para validação de duplicatas)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Busca produto por nome exato (case insensitive)
     */
    Optional<Product> findByNameIgnoreCase(String name);

    /**
     * Busca os 10 produtos mais caros
     */
    @Query("SELECT p FROM Product p ORDER BY p.price DESC")
    List<Product> findTop10ByOrderByPriceDesc(Pageable pageable);

    /**
     * Busca os 10 produtos mais baratos
     */
    @Query("SELECT p FROM Product p ORDER BY p.price ASC")
    List<Product> findTop10ByOrderByPriceAsc(Pageable pageable);
}
