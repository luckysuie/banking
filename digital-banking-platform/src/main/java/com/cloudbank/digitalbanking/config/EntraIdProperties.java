package com.cloudbank.digitalbanking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.entra")
public class EntraIdProperties {

    private String tenantId;
    private String clientId;
    private String rolesClaim = "roles";
}
