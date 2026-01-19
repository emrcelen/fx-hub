package com.emrecelen.rateproducer.domain.service;

import com.emrecelen.rateproducer.common.Constants.OutboxType;
import com.emrecelen.rateproducer.domain.model.MoneyScale;
import com.emrecelen.rateproducer.domain.model.RateEvent;
import com.emrecelen.rateproducer.domain.model.RawRate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RateEventFactory implements DomainEventFactory<RawRate> {


    @Override
    public String eventType() {
        return OutboxType.RATE_EVENT.name();
    }

    @Override
    public int schemaVersion() {
        return 1;
    }

    @Override
    public String eventKey(RawRate raw) {
        return raw.source().concat(":").concat(raw.pair()).concat(":").concat(String.valueOf(raw.seq()));
    }

    @Override
    public RateEvent createEvent(RawRate raw) {
        long bid = MoneyScale.toPips(raw.bid());
        long ask = MoneyScale.toPips(raw.ask());
        return new RateEvent(
                eventKey(raw),
                schemaVersion(),
                LocalDateTime.now(),
                raw.source(),
                raw.pair(),
                raw.seq(),
                bid,
                ask
        );
    }
}
