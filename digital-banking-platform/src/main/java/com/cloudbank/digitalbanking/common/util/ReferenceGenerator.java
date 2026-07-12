package com.cloudbank.digitalbanking.common.util;

import com.cloudbank.digitalbanking.common.constants.BankingConstants;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Utility for generating unique, non-sequential banking references.
 * Uses UUID and {@link SecureRandom} to avoid predictable identifiers.
 */
public final class ReferenceGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String NUMERIC_CHARS = "0123456789";

    private ReferenceGenerator() {
    }

    /**
     * Generates a new random UUID suitable for use as an internal entity identifier.
     */
    public static UUID generateUuid() {
        return UUID.randomUUID();
    }

    /**
     * Generates an external-facing customer reference (e.g. {@code CUS-A1B2C3D4E5F6}).
     */
    public static String generateCustomerId() {
        return BankingConstants.CUSTOMER_ID_PREFIX + randomAlphanumericFragment();
    }

    /**
     * Generates a unique account number (e.g. {@code CB4829103756}).
     */
    public static String generateAccountNumber() {
        return BankingConstants.ACCOUNT_NUMBER_PREFIX + randomNumeric(BankingConstants.ACCOUNT_NUMBER_LENGTH);
    }

    /**
     * Generates a unique payment reference (e.g. {@code PAY-A1B2C3D4E5F6}).
     */
    public static String generatePaymentReference() {
        return BankingConstants.PAYMENT_REFERENCE_PREFIX + randomAlphanumericFragment();
    }

    /**
     * Generates a unique transaction reference (e.g. {@code TXN-A1B2C3D4E5F6}).
     */
    public static String generateTransactionReference() {
        return BankingConstants.TRANSACTION_REFERENCE_PREFIX + randomAlphanumericFragment();
    }

    /**
     * Generates a unique audit event reference (e.g. {@code AUD-A1B2C3D4E5F6}).
     */
    public static String generateAuditEventReference() {
        return BankingConstants.AUDIT_EVENT_PREFIX + randomAlphanumericFragment();
    }

    private static String randomAlphanumericFragment() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, BankingConstants.REFERENCE_FRAGMENT_LENGTH)
                .toUpperCase();
    }

    private static String randomNumeric(int length) {
        StringBuilder builder = new StringBuilder(length);
        // First digit must not be zero to preserve fixed width semantics
        builder.append(NUMERIC_CHARS.charAt(SECURE_RANDOM.nextInt(9) + 1));
        for (int i = 1; i < length; i++) {
            builder.append(NUMERIC_CHARS.charAt(SECURE_RANDOM.nextInt(NUMERIC_CHARS.length())));
        }
        return builder.toString();
    }
}
