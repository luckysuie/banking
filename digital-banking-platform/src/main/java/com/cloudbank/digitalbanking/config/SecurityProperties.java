package com.cloudbank.digitalbanking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private final UserCredentials customer = new UserCredentials();
    private final UserCredentials admin = new UserCredentials();

    @Getter
    @Setter
    public static class UserCredentials {
        private String username;
        private String password;
    }
}
