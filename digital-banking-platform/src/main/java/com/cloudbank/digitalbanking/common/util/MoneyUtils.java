package com.cloudbank.digitalbanking.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    public static final int MONEY_SCALE = 2;
    public static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private MoneyUtils() {
    }

    public static BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Monetary amount cannot be null");
        }
        return amount.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    public static BigDecimal add(BigDecimal left, BigDecimal right) {
        return normalize(left).add(normalize(right));
    }

    public static BigDecimal subtract(BigDecimal left, BigDecimal right) {
        return normalize(left).subtract(normalize(right));
    }
}
