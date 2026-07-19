package com.learning.job_portal_service.config;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authPortalOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Job Portal Auth API")
                .version("1.0.0")
                .description("Authentication endpoints for the Job Portal"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }

    /**
     * Globally replaces wildcard "*\/*" response content types with
     * "application/json" across every operation in the spec.
     *
     * Springdoc 2.x emits "*\/*" when no explicit mediaType is set on @Content.
     * The Angular OpenAPI generator sees "*\/*" and picks responseType='blob'
     * instead of responseType='json', causing response bodies to arrive as
     * Blob objects rather than parsed JSON.
     *
     * This customizer runs once per operation at spec-build time — zero overhead
     * at request time.
     */
    @Bean
    public OperationCustomizer globalJsonMediaType() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            if (operation.getResponses() == null) return operation;

            operation.getResponses().forEach((statusCode, apiResponse) -> {
                Content content = apiResponse.getContent();
                if (content == null || !content.containsKey("*/*")) return;

                // Move the schema from "*/*" to "application/json"
                MediaType mediaType = content.get("*/*");
                content.remove("*/*");
                content.addMediaType("application/json", mediaType);
            });

            return operation;
        };
    }
}
