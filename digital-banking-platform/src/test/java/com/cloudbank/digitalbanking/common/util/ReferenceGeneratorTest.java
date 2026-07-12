package com.cloudbank.digitalbanking.common.util;

import com.cloudbank.digitalbanking.common.constants.BankingConstants;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceGeneratorTest {

    private static final Pattern CUSTOMER_ID_PATTERN =
            Pattern.compile("^" + BankingConstants.CUSTOMER_ID_PREFIX + "[A-F0-9]{12}$");
    private static final Pattern ACCOUNT_NUMBER_PATTERN =
            Pattern.compile("^" + BankingConstants.ACCOUNT_NUMBER_PREFIX + "[1-9][0-9]{9}$");
    private static final Pattern PAYMENT_REFERENCE_PATTERN =
            Pattern.compile("^" + BankingConstants.PAYMENT_REFERENCE_PREFIX + "[A-F0-9]{12}$");
    private static final Pattern TRANSACTION_REFERENCE_PATTERN =
            Pattern.compile("^" + BankingConstants.TRANSACTION_REFERENCE_PREFIX + "[A-F0-9]{12}$");

    @Test
    void generateCustomerId_shouldMatchExpectedFormat() {
        assertThat(ReferenceGenerator.generateCustomerId()).matches(CUSTOMER_ID_PATTERN);
    }

    @Test
    void generateAccountNumber_shouldMatchExpectedFormat() {
        assertThat(ReferenceGenerator.generateAccountNumber()).matches(ACCOUNT_NUMBER_PATTERN);
    }

    @Test
    void generatePaymentReference_shouldMatchExpectedFormat() {
        assertThat(ReferenceGenerator.generatePaymentReference()).matches(PAYMENT_REFERENCE_PATTERN);
    }

    @Test
    void generateTransactionReference_shouldMatchExpectedFormat() {
        assertThat(ReferenceGenerator.generateTransactionReference()).matches(TRANSACTION_REFERENCE_PATTERN);
    }

    @RepeatedTest(20)
    void generateAccountNumber_shouldProduceUniqueValues() {
        Set<String> generated = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            generated.add(ReferenceGenerator.generateAccountNumber());
        }
        assertThat(generated).hasSize(10);
    }
}
