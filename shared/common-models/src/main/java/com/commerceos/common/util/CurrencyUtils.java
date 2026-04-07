package com.commerceos.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Multi-currency support.
 * All internal amounts stored in paise (INR x 100).
 * Display conversion happens at read time — never stored.
 */
public final class CurrencyUtils {

    private CurrencyUtils() {}

    public static final String BASE_CURRENCY = "INR";

    private static final Map<String, Integer> DECIMAL_PLACES = Map.of(
            "INR", 2,
            "USD", 2,
            "EUR", 2,
            "GBP", 2,
            "JPY", 0,
            "AED", 2
    );

    private static final Map<String, Long> SUBUNIT_FACTOR = Map.of(
            "INR", 100L,
            "USD", 100L,
            "EUR", 100L,
            "GBP", 100L,
            "JPY", 1L,
            "AED", 100L
    );

    public static long toSubunits(BigDecimal amount,
                                   String currency) {
        long factor = SUBUNIT_FACTOR
                .getOrDefault(currency.toUpperCase(), 100L);
        return amount.multiply(BigDecimal.valueOf(factor))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    public static BigDecimal fromSubunits(long subunits,
                                           String currency) {
        long factor = SUBUNIT_FACTOR
                .getOrDefault(currency.toUpperCase(), 100L);
        int decimals = DECIMAL_PLACES
                .getOrDefault(currency.toUpperCase(), 2);
        return BigDecimal.valueOf(subunits)
                .divide(BigDecimal.valueOf(factor),
                        decimals, RoundingMode.HALF_UP);
    }

    public static long convert(long sourcePaise,
                                BigDecimal exchangeRate,
                                String targetCurrency) {
        long factor = SUBUNIT_FACTOR
                .getOrDefault(targetCurrency.toUpperCase(), 100L);
        return BigDecimal.valueOf(sourcePaise)
                .multiply(exchangeRate)
                .divide(BigDecimal.valueOf(100L),
                        0, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(factor))
                .longValue();
    }

    public static String format(long subunits, String currency) {
        BigDecimal amount = fromSubunits(subunits, currency);
        return currency.toUpperCase() + " " + amount.toPlainString();
    }
}
