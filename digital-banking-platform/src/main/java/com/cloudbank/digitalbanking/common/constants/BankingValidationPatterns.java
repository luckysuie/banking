package com.cloudbank.digitalbanking.common.constants;

public final class BankingValidationPatterns {

    /**
     * External Canadian account numbers (7-12 digits) or internal CloudBank numbers (CB + 10 digits).
     */
    public static final String BENEFICIARY_ACCOUNT_NUMBER =
            "^(?:\\d{7,12}|CB\\d{10})$";

    private BankingValidationPatterns() {
    }
}
