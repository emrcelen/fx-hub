package com.emrecelen.rateproducer.domain.model;

public record RawRate(
        String source,
        String pair,
        long seq,
        String bid,
        String ask,
        long timestamp
) {
}
