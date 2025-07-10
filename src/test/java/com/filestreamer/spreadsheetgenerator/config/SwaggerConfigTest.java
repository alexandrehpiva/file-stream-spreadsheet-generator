package com.filestreamer.spreadsheetgenerator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {

    private SwaggerConfig swaggerConfig;

    @BeforeEach
    void setUp() {
        // Given
        swaggerConfig = new SwaggerConfig();
    }

    @Test
    void shouldCreateCustomOpenAPIWithDefaultPort() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "serverPort", "8080");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getServers());
        assertFalse(openAPI.getServers().isEmpty());
        
        Server server = openAPI.getServers().get(0);
        assertEquals("http://localhost:8080", server.getUrl());
        assertEquals("Servidor de desenvolvimento", server.getDescription());
    }

    @Test
    void shouldCreateCustomOpenAPIWithCustomPort() {
        // Given
        String customPort = "9090";
        ReflectionTestUtils.setField(swaggerConfig, "serverPort", customPort);

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI);
        Server server = openAPI.getServers().get(0);
        assertEquals("http://localhost:" + customPort, server.getUrl());
    }

    @Test
    void shouldConfigureApiInfoCorrectly() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "serverPort", "8080");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        Info info = openAPI.getInfo();
        assertNotNull(info);
        assertEquals("Spreadsheet Generator API", info.getTitle());
        assertEquals("1.0.0", info.getVersion());
        assertNotNull(info.getDescription());
        assertTrue(info.getDescription().contains("API para gerenciamento de produtos"));
        assertEquals("http://swagger.io/terms/", info.getTermsOfService());
    }

    @Test
    void shouldConfigureContactInfoCorrectly() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "serverPort", "8080");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        Contact contact = openAPI.getInfo().getContact();
        assertNotNull(contact);
        assertEquals("dev@filestreamer.com", contact.getEmail());
        assertEquals("FileStreamer Team", contact.getName());
        assertEquals("https://github.com/filestreamer", contact.getUrl());
    }

    @Test
    void shouldConfigureLicenseInfoCorrectly() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "serverPort", "8080");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        License license = openAPI.getInfo().getLicense();
        assertNotNull(license);
        assertEquals("MIT License", license.getName());
        assertEquals("https://choosealicense.com/licenses/mit/", license.getUrl());
    }

    @Test
    void shouldHaveOneServerConfigured() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "serverPort", "8080");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getServers());
        assertEquals(1, openAPI.getServers().size());
    }

    @Test
    void shouldHandleNullPort() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "serverPort", null);

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI);
        Server server = openAPI.getServers().get(0);
        assertEquals("http://localhost:null", server.getUrl());
    }

    @Test
    void shouldHandleEmptyPort() {
        // Given
        ReflectionTestUtils.setField(swaggerConfig, "serverPort", "");

        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI);
        Server server = openAPI.getServers().get(0);
        assertEquals("http://localhost:", server.getUrl());
    }
} 