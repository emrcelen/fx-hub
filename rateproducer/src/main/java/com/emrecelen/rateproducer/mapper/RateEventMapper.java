package com.emrecelen.rateproducer.mapper;

import com.emrecelen.rateproducer.api.dto.RawRateRequest;
import com.emrecelen.rateproducer.domain.model.RawRate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RateEventMapper {

    public RawRate toRawRate(
            String source,
            Long seq,
            RawRateRequest request
    ) {
        return new RawRate(
                source,
                request.pair(),
                seq,
                request.bid(),
                request.ask(),
                Instant.now().toEpochMilli()
        );
    }
}
