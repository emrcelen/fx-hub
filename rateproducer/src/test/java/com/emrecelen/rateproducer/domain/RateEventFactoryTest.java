package com.emrecelen.rateproducer.domain;

import com.emrecelen.rateproducer.common.Constants;
import com.emrecelen.rateproducer.domain.model.MoneyScale;
import com.emrecelen.rateproducer.domain.model.RateEvent;
import com.emrecelen.rateproducer.domain.model.RawRate;
import com.emrecelen.rateproducer.domain.service.RateEventFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RateEventFactoryTest {

    private final RateEventFactory factory = new RateEventFactory();

    @Test
    void should_return_rate_event_type() {
        assertThat(factory.eventType())
                .isEqualTo(Constants.OutboxType.RATE_EVENT.name());
    }

    @Test
    void should_return_schema_version_1() {
        assertThat(factory.schemaVersion()).isEqualTo(1);
    }

    @Test
    void should_build_event_key_correctly() {
        RawRate raw = new RawRate(
                "FX",
                "EUR/USD",
                42,
                "1.100001",
                "1.100001",
                1
        );
        String key = factory.eventKey(raw);
        assertThat(key).isEqualTo("FX:EUR/USD:42");
    }

    @Test
    void should_create_rate_event_with_mapped_fields() {
        RawRate raw = new RawRate(
                "FX",
                "EUR/USD",
                10,
                "1.12345",
                "1.56789",
                1
        );

        LocalDateTime before = LocalDateTime.now();

        RateEvent event = factory.createEvent(raw);

        LocalDateTime after = LocalDateTime.now();

        assertThat(event).isNotNull();
        assertThat(event.eventKey()).isEqualTo("FX:EUR/USD:10");
        assertThat(event.schemaVersion()).isEqualTo(1);
        assertThat(event.source()).isEqualTo("FX");
        assertThat(event.pair()).isEqualTo("EUR/USD");
        assertThat(event.seq()).isEqualTo(10);

        assertThat(event.bidPips()).isEqualTo(MoneyScale.toPips(raw.bid()));
        assertThat(event.askPips()).isEqualTo(MoneyScale.toPips(raw.ask()));

        assertThat(event.producedAt())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }


}
