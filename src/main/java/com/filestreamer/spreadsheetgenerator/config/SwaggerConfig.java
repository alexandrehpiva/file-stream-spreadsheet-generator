package com.filestreamer.spreadsheetgenerator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do Swagger/OpenAPI
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription("Servidor de desenvolvimento");

        Contact contact = new Contact();
        contact.setEmail("dev@filestreamer.com");
        contact.setName("FileStreamer Team");
        contact.setUrl("https://github.com/filestreamer");

        License license = new License();
        license.setName("MIT License");
        license.setUrl("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Spreadsheet Generator API")
                .version("1.0.0")
                .contact(contact)
                .description("API para gerenciamento de produtos com funcionalidades de leitura de planilhas e exportação CSV. " +
                        "Esta API permite criar, listar, atualizar e deletar produtos, além de realizar exportações em formato CSV.")
                .termsOfService("http://swagger.io/terms/")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
