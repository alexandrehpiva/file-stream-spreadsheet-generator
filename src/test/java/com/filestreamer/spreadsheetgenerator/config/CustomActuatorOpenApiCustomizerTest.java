package com.filestreamer.spreadsheetgenerator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.argThat;

class CustomActuatorOpenApiCustomizerTest {

    private CustomActuatorOpenApiCustomizer customizer;
    private OpenAPI openAPI;
    private Paths paths;

    @BeforeEach
    void setUp() {
        // Given
        customizer = new CustomActuatorOpenApiCustomizer();
        openAPI = mock(OpenAPI.class);
        paths = mock(Paths.class);
    }

    @Test
    void shouldSkipCustomizationWhenPathsIsNull() {
        // Given
        when(openAPI.getPaths()).thenReturn(null);

        // When
        customizer.customise(openAPI);

        // Then
        verify(openAPI, only()).getPaths();
    }

    @Test
    void shouldCustomizeAllActuatorEndpointsWhenPathsExists() {
        // Given
        when(openAPI.getPaths()).thenReturn(paths);
        when(paths.get(anyString())).thenReturn(null);

        // When
        customizer.customise(openAPI);

        // Then
        verify(openAPI, times(6)).getPaths();
        verify(paths, atLeastOnce()).get(anyString());
    }

    @Test
    void shouldCustomizeActuatorRootEndpoint() {
        // Given
        PathItem pathItem = createMockPathItem();
        Operation operation = pathItem.getGet();
        when(openAPI.getPaths()).thenReturn(paths);
        when(paths.get("/actuator")).thenReturn(pathItem);

        // When
        customizer.customise(openAPI);

        // Then
        verify(operation).setSummary("Lista todos os endpoints disponíveis do Actuator");
        verify(operation).setDescription(argThat(desc -> desc.contains("Retorna uma lista de todos os endpoints de monitoramento")));
        verify(operation).setResponses(any(ApiResponses.class));
    }

    @Test
    void shouldCustomizeActuatorHealthEndpoint() {
        // Given
        PathItem pathItem = createMockPathItem();
        Operation operation = pathItem.getGet();
        when(openAPI.getPaths()).thenReturn(paths);
        when(paths.get("/actuator/health")).thenReturn(pathItem);

        // When
        customizer.customise(openAPI);

        // Then
        verify(operation).setSummary("Verifica o status de saúde da aplicação");
        verify(operation).setDescription(argThat(desc -> desc.contains("Endpoint principal para health checks")));
        verify(operation).setResponses(any(ApiResponses.class));
    }

    @Test
    void shouldCustomizeActuatorInfoEndpoint() {
        // Given
        PathItem pathItem = createMockPathItem();
        Operation operation = pathItem.getGet();
        when(openAPI.getPaths()).thenReturn(paths);
        when(paths.get("/actuator/info")).thenReturn(pathItem);

        // When
        customizer.customise(openAPI);

        // Then
        verify(operation).setSummary("Informações gerais da aplicação");
        verify(operation).setDescription(argThat(desc -> desc.contains("Retorna informações customizáveis sobre a aplicação")));
        verify(operation).setResponses(any(ApiResponses.class));
    }

    @Test
    void shouldCustomizeActuatorMetricsEndpoint() {
        // Given
        PathItem pathItem = createMockPathItem();
        Operation operation = pathItem.getGet();
        when(openAPI.getPaths()).thenReturn(paths);
        when(paths.get("/actuator/metrics")).thenReturn(pathItem);

        // When
        customizer.customise(openAPI);

        // Then
        verify(operation).setSummary("Lista todas as métricas disponíveis");
        verify(operation).setDescription(argThat(desc -> desc.contains("Retorna uma lista com nomes de todas as métricas")));
        verify(operation).setResponses(any(ApiResponses.class));
    }

    @Test
    void shouldCustomizeActuatorMetricsSpecificEndpoint() {
        // Given
        PathItem pathItem = createMockPathItem();
        Operation operation = pathItem.getGet();
        Parameter parameter = mock(Parameter.class);
        when(operation.getParameters()).thenReturn(List.of(parameter));
        
        when(openAPI.getPaths()).thenReturn(paths);
        when(paths.get("/actuator/metrics/{requiredMetricName}")).thenReturn(pathItem);

        // When
        customizer.customise(openAPI);

        // Then
        verify(operation).setSummary("Obtém valores de uma métrica específica");
        verify(operation).setDescription(argThat(desc -> desc.contains("Retorna os valores atuais de uma métrica específica")));
        verify(operation).setResponses(any(ApiResponses.class));
        verify(parameter).setDescription(anyString());
        verify(parameter).setExample("jvm.memory.used");
    }

    @Test
    void shouldHandleNullPathItemForActuatorRoot() {
        // Given
        when(openAPI.getPaths()).thenReturn(paths);
        when(paths.get("/actuator")).thenReturn(null);

        // When
        customizer.customise(openAPI);

        // Then
        verify(paths).get("/actuator");
        // Should not throw exception and continue processing
    }

    @Test
    void shouldHandlePathItemWithNullGetOperation() {
        // Given
        PathItem pathItem = mock(PathItem.class);
        when(pathItem.getGet()).thenReturn(null);
        when(openAPI.getPaths()).thenReturn(paths);
        when(paths.get("/actuator")).thenReturn(pathItem);

        // When
        customizer.customise(openAPI);

        // Then
        verify(pathItem).getGet();
        // Should not throw exception
    }

    @Test
    void shouldCreateStandardResponsesWithCorrectStatusCodes() {
        // Given
        PathItem pathItem = createMockPathItem();
        Operation operation = pathItem.getGet();
        when(openAPI.getPaths()).thenReturn(paths);
        when(paths.get("/actuator")).thenReturn(pathItem);

        // When
        customizer.customise(openAPI);

        // Then
        verify(operation).setResponses(argThat(responses -> 
            responses != null && 
            responses.containsKey("200") && 
            responses.containsKey("404") && 
            responses.containsKey("503")
        ));
    }

    @Test
    void shouldSetCorrectResponseDescriptions() {
        // Given
        PathItem pathItem = createMockPathItem();
        Operation operation = pathItem.getGet();
        when(openAPI.getPaths()).thenReturn(paths);
        when(paths.get("/actuator/health")).thenReturn(pathItem);

        // When
        customizer.customise(openAPI);

        // Then
        verify(operation).setResponses(argThat(responses -> {
            if (responses == null) return false;
            
            ApiResponse notFoundResponse = responses.get("404");
            ApiResponse unavailableResponse = responses.get("503");
            
            return notFoundResponse != null && 
                   "Métrica não encontrada ou endpoint não disponível".equals(notFoundResponse.getDescription()) &&
                   unavailableResponse != null && 
                   "Serviço indisponível - aplicação em estado DOWN".equals(unavailableResponse.getDescription());
        }));
    }

    @Test
    void shouldHandleMetricsEndpointWithoutParameters() {
        // Given
        PathItem pathItem = createMockPathItem();
        Operation operation = pathItem.getGet();
        when(operation.getParameters()).thenReturn(null);
        
        when(openAPI.getPaths()).thenReturn(paths);
        when(paths.get("/actuator/metrics/{requiredMetricName}")).thenReturn(pathItem);

        // When
        customizer.customise(openAPI);

        // Then
        // Should not throw exception when parameters is null
        verify(operation).setResponses(any(ApiResponses.class));
    }

    @Test
    void shouldHandleMetricsEndpointWithEmptyParameters() {
        // Given
        PathItem pathItem = createMockPathItem();
        Operation operation = pathItem.getGet();
        when(operation.getParameters()).thenReturn(List.of());
        
        when(openAPI.getPaths()).thenReturn(paths);
        when(paths.get("/actuator/metrics/{requiredMetricName}")).thenReturn(pathItem);

        // When
        customizer.customise(openAPI);

        // Then
        // Should not throw exception when parameters is empty
        verify(operation).setResponses(any(ApiResponses.class));
    }

    private void setupMockPaths() {
        when(paths.get("/actuator")).thenReturn(createMockPathItem());
        when(paths.get("/actuator/health")).thenReturn(createMockPathItem());
        when(paths.get("/actuator/info")).thenReturn(createMockPathItem());
        when(paths.get("/actuator/metrics")).thenReturn(createMockPathItem());
        when(paths.get("/actuator/metrics/{requiredMetricName}")).thenReturn(createMockPathItem());
    }

    private PathItem createMockPathItem() {
        PathItem pathItem = mock(PathItem.class);
        Operation operation = mock(Operation.class);
        ApiResponses responses = new ApiResponses();
        
        when(pathItem.getGet()).thenReturn(operation);
        when(operation.getResponses()).thenReturn(responses);
        doNothing().when(operation).setSummary(anyString());
        doNothing().when(operation).setDescription(anyString());
        doNothing().when(operation).setResponses(any(ApiResponses.class));
        
        return pathItem;
    }
} 