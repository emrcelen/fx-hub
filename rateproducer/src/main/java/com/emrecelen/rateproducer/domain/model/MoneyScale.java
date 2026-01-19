package com.emrecelen.rateproducer.domain.model;

import java.math.BigDecimal;

public final class MoneyScale {
    private final static int MOVE_POINT_VALUE = 5;

    private MoneyScale() {
    }

    public static long toPips(String value) {
        if (value == null) {
            return BigDecimal.ZERO
                    .movePointRight(MOVE_POINT_VALUE)
                    .longValueExact();
        }
        return new BigDecimal(value)
                .movePointRight(MOVE_POINT_VALUE)
                .longValueExact();
    }
}
