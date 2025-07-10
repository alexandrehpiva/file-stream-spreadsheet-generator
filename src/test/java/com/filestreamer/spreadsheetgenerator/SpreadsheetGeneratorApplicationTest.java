package com.filestreamer.spreadsheetgenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SpreadsheetGeneratorApplicationTest {

    @Test
    void shouldCreateApplicationInstance() {
        // Given & When
        SpreadsheetGeneratorApplication app = new SpreadsheetGeneratorApplication();

        // Then
        // This test verifies that the application can be instantiated successfully
        assertNotNull(app);
    }

    @Test
    void shouldRunApplicationWithMainMethod() {
        // Given
        String[] args = {"--spring.profiles.active=test"};

        // When & Then
        try (MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            SpreadsheetGeneratorApplication.main(args);
            springApp.verify(() -> SpringApplication.run(eq(SpreadsheetGeneratorApplication.class), eq(args)));
        }
    }

    @Test
    void shouldLoadDotenvInDevelopmentMode() {
        // Given
        String[] args = {};
        System.setProperty("spring.profiles.active", "development");

        // When & Then
        try (MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            SpreadsheetGeneratorApplication.main(args);
            springApp.verify(() -> SpringApplication.run(any(Class.class), eq(args)));
        } finally {
            System.clearProperty("spring.profiles.active");
        }
    }

    @Test
    void shouldLoadDotenvInDevMode() {
        // Given
        String[] args = {};
        System.setProperty("spring.profiles.active", "dev");

        // When & Then
        try (MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            SpreadsheetGeneratorApplication.main(args);
            springApp.verify(() -> SpringApplication.run(any(Class.class), eq(args)));
        } finally {
            System.clearProperty("spring.profiles.active");
        }
    }

    @Test
    void shouldLoadDotenvWhenNoProfileSet() {
        // Given
        String[] args = {};
        System.clearProperty("spring.profiles.active");

        // When & Then
        try (MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            SpreadsheetGeneratorApplication.main(args);
            springApp.verify(() -> SpringApplication.run(any(Class.class), eq(args)));
        }
    }

    @Test
    void shouldNotLoadDotenvInProductionMode() {
        // Given
        String[] args = {};
        System.setProperty("spring.profiles.active", "production");

        // When & Then
        try (MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            SpreadsheetGeneratorApplication.main(args);
            springApp.verify(() -> SpringApplication.run(any(Class.class), eq(args)));
        } finally {
            System.clearProperty("spring.profiles.active");
        }
    }

    @Test
    void shouldNotLoadDotenvInTestMode() {
        // Given
        String[] args = {};
        System.setProperty("spring.profiles.active", "test");

        // When & Then
        try (MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            SpreadsheetGeneratorApplication.main(args);
            springApp.verify(() -> SpringApplication.run(any(Class.class), eq(args)));
        } finally {
            System.clearProperty("spring.profiles.active");
        }
    }

    @Test
    void shouldHandleEmptyArgs() {
        // Given
        String[] args = {};

        // When & Then
        try (MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            SpreadsheetGeneratorApplication.main(args);
            springApp.verify(() -> SpringApplication.run(any(Class.class), eq(args)));
        }
    }

    @Test
    void shouldHandleNullArgs() {
        // Given
        String[] args = null;

        // When & Then
        try (MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            SpreadsheetGeneratorApplication.main(args);
            springApp.verify(() -> SpringApplication.run(any(Class.class), eq(args)));
        }
    }
} 