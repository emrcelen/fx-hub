package com.emrecelen.rate_hub.util;

import com.emrecelen.rate_hub.model.RateEvent;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Component
public class RateEventParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public Optional<RateEvent> parse(String raw) {
        if (raw == null || raw.isBlank()) return Optional.empty();

        try {
            JsonNode n = mapper.readTree(raw);
            if (n.isString()) {
                n = mapper.readTree(n.asString());
            }
            if (!n.hasNonNull("eventKey")) return Optional.empty();
            if (!n.hasNonNull("schemaVersion")) return Optional.empty();
            if (!n.hasNonNull("producedAt")) return Optional.empty();
            if (!n.hasNonNull("source")) return Optional.empty();
            if (!n.hasNonNull("pair")) return Optional.empty();
            if (!n.hasNonNull("seq")) return Optional.empty();
            if (!n.hasNonNull("bidPips")) return Optional.empty();
            if (!n.hasNonNull("askPips")) return Optional.empty();

            return Optional.of(mapper.treeToValue(n, RateEvent.class));
        } catch (Exception ignore) {
            return Optional.empty();
        }
    }
}
