package com.emrecelen.rate_hub.util;

import com.emrecelen.rate_hub.model.RateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class RateEventValidator {

    /**
     * Valid currency pair format:
     * <pre>
     * EUR/USD
     * EUR/USDm
     * USD/TRYSPOT
     * </pre>
     */
    private static final Pattern PAIR_PATTERN =
            Pattern.compile("^[A-Z]{3}/[A-Z]{3}([A-Z]{1,4})?$");
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());


    /**
     * Validates domain-level integrity of a {@link RateEvent}.
     *
     * <p>
     * This validation ensures that the event is safe to be processed
     * and stored in the rate snapshot store.
     *
     * <p>
     * This method never throws an exception; invalid events are simply rejected.
     *
     * @param e incoming rate event
     * @return {@code true} if event is domain-valid, otherwise {@code false}
     */
    public boolean isValid(RateEvent e) {
        if (e == null) {
            log.debug("RateEvent validation failed: event is null");
            return false;
        }
        if (!isValidPair(e.pair())) {
            log.warn("Invalid currency pair format. pair={}", e.pair());
            return false;
        }
        if (e.seq() <= 0) {
            log.warn("Invalid sequence number. pair={}, seq={}", e.pair(), e.seq());
            return false;
        }
        if (e.eventKey() == null || e.eventKey().isBlank()) {
            log.warn("Missing or empty eventKey. pair={}", e.pair());
            return false;
        }
        if (e.schemaVersion() != 1) {
            log.warn(
                    "Unsupported schema version. pair={}, version={}",
                    e.pair(),
                    e.schemaVersion()
            );
            return false;
        }
        if (e.askPips() < 0) {
            log.warn(
                    "Invalid ask price. pair={}, askPips={}",
                    e.pair(),
                    e.askPips()
            );
            return false;
        }
        if (e.bidPips() < 0) {
            log.warn(
                    "Invalid bid price. pair={}, bidPips={}",
                    e.pair(),
                    e.bidPips()
            );
            return false;
        }
        if (e.bidPips() >= e.askPips()) {
            log.warn(
                    "Bid price must be lower than ask price. pair={}, bid={}, ask={}",
                    e.pair(),
                    e.bidPips(),
                    e.askPips()
            );
            return false;
        }
        log.debug(
                "RateEvent validation passed. pair={}, seq={}",
                e.pair(),
                e.seq()
        );
        return true;
    }

    /**
     * Checks whether currency pair matches expected format.
     */
    private boolean isValidPair(String pair) {
        return pair != null
                && !pair.isBlank()
                && PAIR_PATTERN.matcher(pair).matches();
    }
}
