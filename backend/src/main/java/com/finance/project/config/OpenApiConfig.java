package com.finance.project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI financeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Personal Finance Management API")
                        .description("Servicios REST para la gestion de finanzas personales y grupales: " +
                                "personas, grupos, cuentas, categorias y libros de transacciones (ledgers).")
                        .version("v1.0.0")
                        .license(new License().name("MIT")));
    }
}
