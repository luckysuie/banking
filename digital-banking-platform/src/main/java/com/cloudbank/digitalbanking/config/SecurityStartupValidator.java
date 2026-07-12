package com.cloudbank.digitalbanking.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@Profile("!azure")
@RequiredArgsConstructor
public class SecurityStartupValidator {

    private static final Set<String> INSECURE_DEFAULT_PASSWORDS = Set.of(
            "changeme-customer",
            "changeme-admin");

    private final SecurityProperties securityProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void validateSecurityConfiguration() {
        warnIfDefaultPassword(securityProperties.getCustomer().getPassword(), "customer");
        warnIfDefaultPassword(securityProperties.getAdmin().getPassword(), "admin");
    }

    private void warnIfDefaultPassword(String password, String accountType) {
        if (password != null && INSECURE_DEFAULT_PASSWORDS.contains(password)) {
            log.warn(
                    "Insecure default {} password detected. Set APP_SECURITY_{}_PASSWORD before production deployment.",
                    accountType,
                    accountType.toUpperCase());
        }
    }
}
