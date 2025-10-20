package com.teclab.practicas.WikiBackend.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.hibernate.Hibernate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Configuration
@SecurityScheme(
    name = "Bearer Authentication", // Nombre de tu esquema
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WikiBackend") // 🎯 Título de tu API
                        .version("0.1") // 🎯 Versión de la API
                        .description(
                                """
                                        Esta es la API backend que soporta la Plataforma de Capacitacion y Documentacion,
                                        ofreciendo servicios RESTful seguros con Spring Boot 3.5.4 y Java 21.
                                        La plataforma implementa un robusto sistema de autorizacion basado en roles (SuperUser, Admin, Colaborador)
                                        mediante Spring Security y JWT para el control de acceso a la gestion de contenido jerarquico y la generacion de menus dinamicos.
                                        La arquitectura sigue un patron de capas, optimizando la persistencia con JPA/Hibernate y
                                        garantizando respuestas coherentes mediante el manejo global de excepciones con @RestControllerAdvice."""
                        ));
    }

}