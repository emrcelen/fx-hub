package com.emrecelen.rate_hub.model;

import java.time.LocalDateTime;

public record RateEvent(
        String eventKey,
        int schemaVersion,
        LocalDateTime producedAt,
        String source,
        String pair,
        long seq,
        long bidPips,
        long askPips
) {
}