package com.cloudbank.digitalbanking.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Shared authorization rules for HTTP Basic auth (all profiles).
 *
 * <p>The backend REST API is namespaced under {@code /api/**}; everything else
 * (the React single-page application shell, its static assets, and client-side
 * routes such as {@code /login} or {@code /dashboard}) is permitted here and
 * served by {@link SpaWebConfig}. The SPA itself enforces its own client-side
 * route guarding, so gating page navigation at the security filter chain is
 * unnecessary — only the underlying {@code /api/**} calls need to be secured.
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
                .requestMatchers("/", "/index.html", "/assets/**").permitAll()
                .requestMatchers("/api/audit-events/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/customers").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/accounts/*/status").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll();
    }
}
