package com.cloudbank.digitalbanking.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Shared authorization rules for HTTP Basic auth (all profiles).
 */
public final class SecurityRules {

    private SecurityRules() {
    }

    public static void apply(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth,
            boolean permitH2Console) {
        if (permitH2Console) {
            auth.requestMatchers("/h2-console/**").permitAll();
        }
        auth.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**")
                .permitAll()
                .requestMatchers("/audit-events/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/customers").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/accounts/*/status").hasRole("ADMIN")
                .anyRequest().authenticated();
    }
}
