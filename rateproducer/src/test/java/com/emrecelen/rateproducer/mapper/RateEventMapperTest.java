package com.emrecelen.rateproducer.mapper;

import com.emrecelen.rateproducer.api.dto.RawRateRequest;
import com.emrecelen.rateproducer.domain.model.RawRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RateEventMapperTest {

    private RateEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RateEventMapper();
    }

    @Test
    void should_map_raw_rate_correctly() {
        // given
        String source = "FX";
        Long seq = 42L;

        RawRateRequest request = new RawRateRequest(
                "EUR/USD",
                "1.0850",
                "1.0870"
        );

        long before = Instant.now().toEpochMilli();

        RawRate result = mapper.toRawRate(source, seq, request);

        long after = Instant.now().toEpochMilli();

        assertThat(result).isNotNull();
        assertThat(result.source()).isEqualTo("FX");
        assertThat(result.pair()).isEqualTo("EUR/USD");
        assertThat(result.seq()).isEqualTo(42L);
        assertThat(result.bid()).isEqualTo("1.0850");
        assertThat(result.ask()).isEqualTo("1.0870");
        assertThat(result.timestamp())
                .isBetween(before, after);
    }

}
