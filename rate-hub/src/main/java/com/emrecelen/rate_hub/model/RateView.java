package com.emrecelen.rate_hub.model;

public record RateView(
        String pair,
        long seq,
        String bid,
        String ask,
        long timestamp
) {
}
