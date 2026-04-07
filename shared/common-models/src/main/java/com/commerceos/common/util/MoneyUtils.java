package com.commerceos.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * All monetary values are stored and computed in paise (1/100 of a rupee).
 * NEVER use double or float for money.
 */
public final class MoneyUtils {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private MoneyUtils() {}

    public static long rupeeToPane(BigDecimal rupees) {
        return rupees.multiply(HUNDRED)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    public static BigDecimal paiseToRupee(long paise) {
        return BigDecimal.valueOf(paise)
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    public static long addPaise(long a, long b) {
        return Math.addExact(a, b);
    }

    public static long subtractPaise(long a, long b) {
        if (b > a) throw new IllegalArgumentException(
                "Subtraction would result in negative amount");
        return a - b;
    }

    public static long percentOf(long paise, BigDecimal ratePercent) {
        return BigDecimal.valueOf(paise)
                .multiply(ratePercent)
                .divide(HUNDRED, 0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    public static long taxFromInclusive(long grossPaise, BigDecimal ratePercent) {
        BigDecimal divisor = BigDecimal.ONE.add(
                ratePercent.divide(HUNDRED, 10, RoundingMode.HALF_UP));
        BigDecimal base = BigDecimal.valueOf(grossPaise)
                .divide(divisor, 0, RoundingMode.HALF_UP);
        return grossPaise - base.longValueExact();
    }
}
