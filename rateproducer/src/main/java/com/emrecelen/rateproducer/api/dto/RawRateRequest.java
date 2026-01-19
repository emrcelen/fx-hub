package com.emrecelen.rateproducer.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RawRateRequest(
        @NotBlank(message = "pair must not be blank")
        @Size(min = 6, max = 12, message = "pair length must be between {min} and {max}")
        @Pattern(regexp = "^[A-Z]{3}/[A-Z]{3}([A-Z]{1,4})?$",
                message = "pair format must be like EUR/USD or BTC/USDT")
        String pair,
        @NotNull
        @Pattern(
                regexp = "\\d+(\\.\\d+)?",
                message = "bid must be a decimal number"
        )
        String bid,
        @NotNull
        @Pattern(
                regexp = "\\d+(\\.\\d+)?",
                message = "ask must be a decimal number"
        )
        String ask
) {
}
