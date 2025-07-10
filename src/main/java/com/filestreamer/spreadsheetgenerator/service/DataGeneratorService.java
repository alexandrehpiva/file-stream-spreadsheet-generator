package com.filestreamer.spreadsheetgenerator.service;

import com.filestreamer.spreadsheetgenerator.model.Product;
import com.filestreamer.spreadsheetgenerator.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class DataGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(DataGeneratorService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    // Arrays para gerar nomes aleatórios
    private static final String[] PRODUCT_PREFIXES = {
        "Smartphone", "Notebook", "Tablet", "Monitor", "Teclado", "Mouse", "Headset", "Webcam",
        "Impressora", "Scanner", "Roteador", "Switch", "Cabo", "Carregador", "Bateria", "Memória",
        "Processador", "Placa", "Cooler", "Fonte", "Gabinete", "SSD", "HD", "Pendrive",
        "Smartwatch", "Fone", "Caixa", "Microfone", "Tripé", "Lente", "Câmera", "Drone",
        "Console", "Controle", "Jogo", "Cadeira", "Mesa", "Suporte", "Luminária", "Ventilador"
    };
    
    private static final String[] PRODUCT_SUFFIXES = {
        "Pro", "Max", "Ultra", "Plus", "Premium", "Deluxe", "Advanced", "Professional",
        "Gaming", "Business", "Home", "Office", "Wireless", "Bluetooth", "USB", "Type-C",
        "4K", "HD", "Full HD", "RGB", "LED", "OLED", "IPS", "Curved", "Portable",
        "Mini", "Compact", "Extended", "Mechanical", "Optical", "Ergonomic", "Adjustable"
    };
    
    private static final String[] BRANDS = {
        "TechCorp", "InnovaSys", "DigitalPro", "SmartTech", "UltraDevices", "ProGaming",
        "OfficeMax", "HomeTech", "BusinessPro", "GamerZone", "TechElite", "InnovaMax",
        "DigitalUltra", "SmartPro", "TechSolutions", "InnovaCore", "ProSystems", "UltraTech"
    };
    
    private static final String[] CATEGORIES = {
        "Informática", "Eletrônicos", "Gaming", "Escritório", "Casa Inteligente", "Áudio e Vídeo",
        "Smartphones", "Acessórios", "Componentes", "Periféricos", "Notebooks", "Monitores"
    };

    @Transactional
    public void generateRandomProducts(int quantity) {
        logger.info("Iniciando geração de {} produtos aleatórios", quantity);
        
        long startTime = System.currentTimeMillis();
        int batchSize = 1000;
        
        for (int i = 0; i < quantity; i += batchSize) {
            int currentBatchSize = Math.min(batchSize, quantity - i);
            List<Product> products = generateProductBatch(currentBatchSize);
            
            productRepository.saveAll(products);
            
            if ((i + currentBatchSize) % 10000 == 0 || (i + currentBatchSize) == quantity) {
                logger.info("Progresso: {}/{} produtos inseridos", i + currentBatchSize, quantity);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        logger.info("Geração concluída! {} produtos inseridos em {}ms ({} produtos/segundo)", 
                quantity, duration, Math.round((double) quantity / duration * 1000));
    }
    
    private List<Product> generateProductBatch(int batchSize) {
        List<Product> products = new ArrayList<>(batchSize);
        Random random = ThreadLocalRandom.current();
        
        for (int i = 0; i < batchSize; i++) {
            Product product = generateRandomProduct(random);
            products.add(product);
        }
        
        return products;
    }
    
    private Product generateRandomProduct(Random random) {
        Product product = new Product();
        
        // Gera nome do produto
        String prefix = PRODUCT_PREFIXES[random.nextInt(PRODUCT_PREFIXES.length)];
        String suffix = PRODUCT_SUFFIXES[random.nextInt(PRODUCT_SUFFIXES.length)];
        String brand = BRANDS[random.nextInt(BRANDS.length)];
        
        product.setName(brand + " " + prefix + " " + suffix);
        
        // Gera descrição com categoria
        String category = CATEGORIES[random.nextInt(CATEGORIES.length)];
        product.setDescription(String.format("%s de alta qualidade da categoria %s. " +
                "Produto inovador com tecnologia avançada e design moderno. " +
                "Ideal para uso profissional e pessoal. Marca %s com garantia estendida.", 
                prefix, category, brand));
        
        // Gera preço entre R$ 10,00 e R$ 9.999,99
        double price = 10.0 + (random.nextDouble() * 9989.99);
        product.setPrice(BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP));
        
        LocalDateTime now = LocalDateTime.now();
        // Cria produtos com datas variadas nos últimos 30 dias
        LocalDateTime createdAt = now.minusDays(random.nextInt(30));
        
        product.setCreatedAt(createdAt);
        product.setUpdatedAt(createdAt.plusHours(random.nextInt(24)));
        
        return product;
    }
    
    public long getProductCount() {
        return productRepository.count();
    }
    
    @Transactional
    public void clearAllProducts() {
        logger.info("Removendo todos os produtos do banco de dados");
        long count = productRepository.count();
        productRepository.deleteAll();
        logger.info("Todos os {} produtos foram removidos", count);
    }
} 