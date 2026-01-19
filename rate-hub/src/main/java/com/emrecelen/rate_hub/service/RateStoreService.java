package com.emrecelen.rate_hub.service;

import com.emrecelen.rate_hub.common.Constants.UpdateResult;
import com.emrecelen.rate_hub.config.HazelcastConfig;
import com.emrecelen.rate_hub.model.RateEvent;
import com.emrecelen.rate_hub.model.RateView;
import com.emrecelen.rate_hub.model.StoreUpdate;
import com.emrecelen.rate_hub.util.RateEventParser;
import com.emrecelen.rate_hub.util.RateEventValidator;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RateStoreService {

    private static final int PIP_SCALE = 5;

    private final IMap<String, RateView> rates;
    private final IMap<String, Long> lastSeq;
    private final IMap<String, RateView> freshness;
    private final RateEventParser parser;
    private final RateEventValidator validator;

    private final long snapshotTtlSeconds;
    private final long invalidRefreshTtlSeconds;

    public RateStoreService(
            HazelcastInstance hz,
            RateEventParser parser,
            RateEventValidator validator,
            @Value("${gateway.rates.snapshot-ttl-seconds}") long snapshotTtlSeconds,
            @Value("${gateway.rates.invalid-refresh-ttl-seconds}") long invalidRefreshTtlSeconds
    ) {
        this.rates = hz.getMap(HazelcastConfig.MAP_RATES);
        this.lastSeq = hz.getMap(HazelcastConfig.MAP_LAST_SEQ);
        this.freshness = hz.getMap(HazelcastConfig.MAP_FRESHNESS);
        this.parser = parser;
        this.validator = validator;
        this.snapshotTtlSeconds = snapshotTtlSeconds;
        this.invalidRefreshTtlSeconds = invalidRefreshTtlSeconds;
    }


    public StoreUpdate onRawMessage(String raw) {
        Optional<RateEvent> parsed = parser.parse(raw);
        if (parsed.isEmpty()) {
            return StoreUpdate.dropped(UpdateResult.DROPPED_INVALID_TRANSPORT);
        }
        return onEvent(parsed.get());
    }

    public StoreUpdate onEvent(RateEvent e) {
        final String pair = e.pair();

        if (pair == null || pair.isBlank()) {
            return StoreUpdate.dropped(UpdateResult.DROPPED_INVALID_SCHEMA);
        }

        lastSeq.lock(pair);
        try {
            if (!validator.isValid(e)) {
                RateView existing = rates.get(pair);
                if (existing != null) {
                    rates.set(pair, existing);
                    freshness.set(pair, existing, invalidRefreshTtlSeconds, TimeUnit.SECONDS);
                    return StoreUpdate.kept(UpdateResult.KEPT_LAST_GOOD_TTL_REFRESHED, existing);
                }
                return StoreUpdate.dropped(UpdateResult.DROPPED_INVALID_DOMAIN);
            }

            Long currentSeq = lastSeq.get(pair);
            if (currentSeq != null && e.seq() <= currentSeq) {
                RateView existing = rates.get(pair);
                if (existing != null) {
                    rates.set(pair, existing);
                    freshness.set(pair, existing, invalidRefreshTtlSeconds, TimeUnit.SECONDS);
                    return StoreUpdate.kept(UpdateResult.DROPPED_OLD_SEQ, existing);
                }
                return StoreUpdate.dropped(UpdateResult.DROPPED_OLD_SEQ);
            }

            lastSeq.put(pair, e.seq());

            RateView view = new RateView(
                    pair,
                    e.seq(),
                    parseRate(e.bidPips()),
                    parseRate(e.askPips()),
                    e.producedAt()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
            );
            rates.set(pair, view);
            freshness.set(pair, view, snapshotTtlSeconds, TimeUnit.SECONDS);
            return StoreUpdate.accepted(UpdateResult.ACCEPTED_UPDATED, view);

        } finally {
            lastSeq.unlock(pair);
        }
    }

    public RateView get(String pair) {
        return rates.get(pair);
    }

    public List<RateView> getAll() {
        return rates.values().stream().toList();
    }

    private String parseRate(long value) {
        try {
            BigDecimal bd = BigDecimal.valueOf(value);
            if (bd.signum() <= 0) return null;
            if (bd.scale() > PIP_SCALE) return null;
            return bd.movePointLeft(PIP_SCALE).toPlainString();
        } catch (Exception e) {
            return null;
        }
    }
}
