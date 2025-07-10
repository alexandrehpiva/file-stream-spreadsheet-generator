package com.filestreamer.spreadsheetgenerator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

@Component
public class CustomActuatorOpenApiCustomizer implements OpenApiCustomizer {

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getPaths() != null) {
            customizeActuatorEndpoints(openApi);
        }
    }

    private void customizeActuatorEndpoints(OpenAPI openApi) {
        // /actuator
        customizeActuatorRoot(openApi);
        
        // /actuator/health
        customizeActuatorHealth(openApi);
        
        // /actuator/info
        customizeActuatorInfo(openApi);
        
        // /actuator/metrics
        customizeActuatorMetrics(openApi);
        
        // /actuator/metrics/{requiredMetricName}
        customizeActuatorMetricsSpecific(openApi);
    }

    private void customizeActuatorRoot(OpenAPI openApi) {
        PathItem pathItem = openApi.getPaths().get("/actuator");
        if (pathItem != null && pathItem.getGet() != null) {
            Operation operation = pathItem.getGet();
            operation.setSummary("Lista todos os endpoints disponíveis do Actuator");
            operation.setDescription("Retorna uma lista de todos os endpoints de monitoramento disponíveis com seus respectivos links. " +
                    "Útil para descobrir quais funcionalidades de monitoring estão habilitadas.");
            
            operation.setResponses(createStandardResponses("Lista de endpoints do Actuator"));
        }
    }

    private void customizeActuatorHealth(OpenAPI openApi) {
        PathItem pathItem = openApi.getPaths().get("/actuator/health");
        if (pathItem != null && pathItem.getGet() != null) {
            Operation operation = pathItem.getGet();
            operation.setSummary("Verifica o status de saúde da aplicação");
            operation.setDescription("Endpoint principal para health checks. Retorna o status geral da aplicação (UP/DOWN) " +
                    "e pode incluir detalhes sobre componentes como banco de dados, disk space, etc. " +
                    "Essencial para monitoramento em produção e load balancers.");
            
            operation.setResponses(createStandardResponses("Status de saúde da aplicação"));
        }
    }

    private void customizeActuatorInfo(OpenAPI openApi) {
        PathItem pathItem = openApi.getPaths().get("/actuator/info");
        if (pathItem != null && pathItem.getGet() != null) {
            Operation operation = pathItem.getGet();
            operation.setSummary("Informações gerais da aplicação");
            operation.setDescription("Retorna informações customizáveis sobre a aplicação como versão, " +
                    "build info, git commit, ambiente, etc. Útil para verificar qual versão está " +
                    "rodando em cada ambiente e para debugging.");
            
            operation.setResponses(createStandardResponses("Informações da aplicação"));
        }
    }

    private void customizeActuatorMetrics(OpenAPI openApi) {
        PathItem pathItem = openApi.getPaths().get("/actuator/metrics");
        if (pathItem != null && pathItem.getGet() != null) {
            Operation operation = pathItem.getGet();
            operation.setSummary("Lista todas as métricas disponíveis");
            operation.setDescription("Retorna uma lista com nomes de todas as métricas coletadas automaticamente " +
                    "pelo Micrometer. Inclui métricas de JVM, HTTP requests, cache, database connections, " +
                    "custom metrics, etc. Use este endpoint para descobrir quais métricas estão disponíveis.");
            
            operation.setResponses(createStandardResponses("Lista de métricas disponíveis"));
        }
    }

    private void customizeActuatorMetricsSpecific(OpenAPI openApi) {
        PathItem pathItem = openApi.getPaths().get("/actuator/metrics/{requiredMetricName}");
        if (pathItem != null && pathItem.getGet() != null) {
            Operation operation = pathItem.getGet();
            operation.setSummary("Obtém valores de uma métrica específica");
            operation.setDescription("Retorna os valores atuais de uma métrica específica. " +
                    "Exemplos úteis: jvm.memory.used, http.server.requests, system.cpu.usage, " +
                    "jdbc.connections.active, etc. Essencial para monitoramento de performance.");
            
            if (operation.getParameters() != null && !operation.getParameters().isEmpty()) {
                operation.getParameters().get(0).setDescription(
                    "Nome da métrica. Exemplos: 'jvm.memory.used', 'http.server.requests', " +
                    "'system.cpu.usage', 'jdbc.connections.active', 'application.ready.time'"
                );
                operation.getParameters().get(0).setExample("jvm.memory.used");
            }
            
            operation.setResponses(createStandardResponses("Valores da métrica específica"));
        }
    }

    private ApiResponses createStandardResponses(String successDescription) {
        ApiResponses responses = new ApiResponses();
        
        // 200 - Success
        ApiResponse successResponse = new ApiResponse();
        successResponse.setDescription(successDescription);
        successResponse.setContent(new Content().addMediaType("application/json", 
            new MediaType().schema(new Schema<>().type("object"))));
        responses.addApiResponse("200", successResponse);
        
        // 404 - Not Found
        ApiResponse notFoundResponse = new ApiResponse();
        notFoundResponse.setDescription("Métrica não encontrada ou endpoint não disponível");
        responses.addApiResponse("404", notFoundResponse);
        
        // 503 - Service Unavailable
        ApiResponse unavailableResponse = new ApiResponse();
        unavailableResponse.setDescription("Serviço indisponível - aplicação em estado DOWN");
        responses.addApiResponse("503", unavailableResponse);
        
        return responses;
    }
}
