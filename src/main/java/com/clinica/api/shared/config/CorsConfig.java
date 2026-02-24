package com.clinica.api.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global CORS.
 * Permite que clientes como Flutter (Web/Mobile), Postman o React/Angular 
 * puedan consumir nuestra API sin ser bloqueados por el navegador.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                // Permitimos todo para desarrollo. En producción se debe restringir por dominio.
                String[] origins = allowedOrigins == null || allowedOrigins.isBlank()
                        ? new String[] { "*" }
                        : allowedOrigins.split("\\s*,\\s*");

                registry.addMapping("/**")
                        .allowedOrigins(origins)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600); // 1 hora de caché para OPTIONS (Preflight)
            }
        };
    }
}
