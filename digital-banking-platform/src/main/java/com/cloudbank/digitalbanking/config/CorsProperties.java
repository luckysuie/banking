package com.cloudbank.digitalbanking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /**
     * Comma-separated list of allowed origin patterns (e.g. https://*.azurewebsites.net).
     */
    private String allowedOriginPatterns = "http://localhost:*,http://127.0.0.1:*";
}
