package com.cloudbank.digitalbanking.common.constants;

import java.math.BigDecimal;

/**
 * Shared constants used across the digital banking platform.
 */
public final class BankingConstants {

    public static final String API_VERSION = "v1";
    public static final String DEFAULT_CURRENCY = "USD";
    public static final String DEFAULT_ACCOUNT_CURRENCY = "CAD";
    public static final String DEFAULT_COUNTRY = "Canada";

    public static final String CUSTOMER_ID_PREFIX = "CUS-";
    public static final String ACCOUNT_NUMBER_PREFIX = "CB";
    public static final String PAYMENT_REFERENCE_PREFIX = "PAY-";
    public static final String TRANSACTION_REFERENCE_PREFIX = "TXN-";
    public static final String AUDIT_EVENT_PREFIX = "AUD-";

    /** Length of the numeric portion of a generated account number. */
    public static final int ACCOUNT_NUMBER_LENGTH = 10;

    /** Length of the random fragment appended to prefixed references. */
    public static final int REFERENCE_FRAGMENT_LENGTH = 12;

    /** Default daily transfer limit for new accounts (CAD). */
    public static final BigDecimal DEFAULT_DAILY_TRANSFER_LIMIT = new BigDecimal("10000.00");

    private BankingConstants() {
    }
}
