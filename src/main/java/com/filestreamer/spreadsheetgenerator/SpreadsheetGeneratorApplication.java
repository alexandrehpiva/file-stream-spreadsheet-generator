package com.filestreamer.spreadsheetgenerator;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpreadsheetGeneratorApplication {

    public static void main(String[] args) {
        // Carregar variáveis do .env apenas em desenvolvimento
        if (isDevelopment()) {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            
            dotenv.entries().forEach(entry -> {
                if (System.getProperty(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
        }
        
        SpringApplication.run(SpreadsheetGeneratorApplication.class, args);
    }
    
    private static boolean isDevelopment() {
        String profile = System.getProperty("spring.profiles.active");
        return profile == null || profile.equals("development") || profile.equals("dev");
    }
} 