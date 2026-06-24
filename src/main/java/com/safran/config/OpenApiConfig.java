package com.safran.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // 1. Appliquer l'exigence de sécurité globalement à tous les endpoints dans l'interface
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // 2. Définir le type de sécurité (JWT Bearer Token)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Entrez votre token JWT sous la forme : Conserver uniquement la chaîne brute (Swagger ajoutera 'Bearer ' automatiquement ou vous demandera de le saisir selon la version).")
                        ));
    }
}