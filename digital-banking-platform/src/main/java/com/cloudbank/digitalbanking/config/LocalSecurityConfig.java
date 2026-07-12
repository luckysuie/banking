package com.cloudbank.digitalbanking.config;

import com.cloudbank.digitalbanking.security.JsonAccessDeniedHandler;
import com.cloudbank.digitalbanking.security.JsonAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Local development security — HTTP Basic with in-memory users.
 */
@Configuration
@EnableWebSecurity
@Profile("!azure")
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
public class LocalSecurityConfig {

    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;
    private final SecurityProperties securityProperties;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> SecurityRules.apply(auth, true))
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails customer = User.builder()
                .username(securityProperties.getCustomer().getUsername())
                .password(passwordEncoder.encode(securityProperties.getCustomer().getPassword()))
                .roles("CUSTOMER")
                .build();

        UserDetails admin = User.builder()
                .username(securityProperties.getAdmin().getUsername())
                .password(passwordEncoder.encode(securityProperties.getAdmin().getPassword()))
                .roles("ADMIN", "CUSTOMER")
                .build();

        return new InMemoryUserDetailsManager(customer, admin);
    }
}
