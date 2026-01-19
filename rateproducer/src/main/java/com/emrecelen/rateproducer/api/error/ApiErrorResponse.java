package com.emrecelen.rateproducer.api.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        String code,
        String message,
        Instant timestamp,
        List<FieldErrorItem> errors
) {
    public record FieldErrorItem(
            String field,
            String message,
            Object rejectedValue
    ) {}
}
