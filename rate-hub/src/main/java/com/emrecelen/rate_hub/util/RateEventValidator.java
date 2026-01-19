package com.emrecelen.rate_hub.util;

import com.emrecelen.rate_hub.model.RateEvent;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class RateEventValidator {

    private static final Pattern PAIR_PATTERN =
            Pattern.compile("^[A-Z]{3}/[A-Z]{3}([A-Z]{1,4})?$");

    public boolean isValid(RateEvent e) {
        if (e == null) return false;

        if (!isValidPair(e.pair())) return false;
        if (e.seq() <= 0) return false;
        if (e.eventKey().isBlank()) return false;
        if (e.schemaVersion() != 1) return false;
        if (e.askPips() < 0) return false;
        if (e.bidPips() >= e.askPips()) return false;
        return e.bidPips() >= 0;
    }

    private boolean isValidPair(String pair) {
        return pair != null
                && !pair.isBlank()
                && PAIR_PATTERN.matcher(pair).matches();
    }
}
