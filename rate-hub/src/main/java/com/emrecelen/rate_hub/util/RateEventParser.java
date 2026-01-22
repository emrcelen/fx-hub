package com.emrecelen.rate_hub.util;

import com.emrecelen.rate_hub.model.RateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Component
public class RateEventParser {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    /**
     * Parses raw incoming message payload into a {@link RateEvent}.
     *
     * <p>
     * This parser is intentionally defensive:
     * <ul>
     *   <li>Handles double-encoded JSON payloads</li>
     *   <li>Validates required schema fields</li>
     *   <li>Never throws exceptions to caller</li>
     * </ul>
     * <p>
     * Invalid or malformed messages are safely dropped.
     *
     * @param raw raw message payload (expected JSON)
     * @return parsed {@link RateEvent} if valid, otherwise {@link Optional#empty()}
     */
    public Optional<RateEvent> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            log.debug("Received empty or null raw message, skipping parse");
            return Optional.empty();
        }


        try {
            JsonNode node = mapper.readTree(raw);
            if (node.isString()) {
                log.trace("Detected stringified JSON payload, unwrapping");
                node = mapper.readTree(node.asString());
            }

            if (!isValidSchema(node)) {
                log.warn(
                        "Invalid RateEvent schema detected, message dropped. payload={}",
                        raw
                );
                return Optional.empty();
            }
            RateEvent event = mapper.treeToValue(node, RateEvent.class);

            log.debug(
                    "RateEvent parsed successfully. pair={}, seq={}",
                    event.pair(),
                    event.seq()
            );
            return Optional.of(event);
        } catch (Exception ex) {
            log.error(
                    "Failed to parse RateEvent payload. payload={}, error={}",
                    raw,
                    ex.getMessage()
            );
            return Optional.empty();
        }
    }

    /**
     * Validates presence of mandatory schema fields.
     */
    private boolean isValidSchema(JsonNode n) {
        return n.hasNonNull("eventKey")
                && n.hasNonNull("schemaVersion")
                && n.hasNonNull("producedAt")
                && n.hasNonNull("source")
                && n.hasNonNull("pair")
                && n.hasNonNull("seq")
                && n.hasNonNull("bidPips")
                && n.hasNonNull("askPips");
    }

}
