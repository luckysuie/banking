package com.cloudbank.digitalbanking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH_SCHEME = "basicAuth";

    @Bean
    public OpenAPI digitalBankingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CloudBank Digital Banking Platform API")
                        .description("REST APIs for the CloudBank digital banking platform. "
                                + "Local development uses HTTP Basic authentication.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("CloudBank Engineering")
                                .email("engineering@cloudbank.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://cloudbank.com")))
                .components(new Components().addSecuritySchemes(BASIC_AUTH_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")
                        .description("HTTP Basic authentication (local development only)")))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH_SCHEME));
    }
}
