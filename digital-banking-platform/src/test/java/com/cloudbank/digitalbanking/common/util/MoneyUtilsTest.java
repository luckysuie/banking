package com.cloudbank.digitalbanking.common.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyUtilsTest {

    @Test
    void normalize_shouldScaleToTwoDecimalPlaces() {
        assertThat(MoneyUtils.normalize(new BigDecimal("10.5")))
                .isEqualByComparingTo("10.50");
    }

    @Test
    void subtract_shouldReturnScaledDifference() {
        assertThat(MoneyUtils.subtract(new BigDecimal("100.00"), new BigDecimal("25.25")))
                .isEqualByComparingTo("74.75");
    }

    @Test
    void normalize_shouldRejectNullAmount() {
        assertThatThrownBy(() -> MoneyUtils.normalize(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }
}
