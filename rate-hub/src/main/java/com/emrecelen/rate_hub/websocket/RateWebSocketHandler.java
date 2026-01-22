package com.emrecelen.rate_hub.websocket;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;

@Component
public class RateWebSocketHandler extends TextWebSocketHandler {

    private final SessionRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public RateWebSocketHandler(SessionRegistry registry) {
        this.registry = registry;
    }

    /**
     * Called when a new WebSocket connection is established.
     * Registers the session in the SessionRegistry.
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        log.info(
                "WebSocket connection established. sessionId={}, remoteAddress={}",
                session.getId(),
                session.getRemoteAddress()
        );
        registry.add(session);
    }

    /**
     * Handles incoming text messages from clients.
     *
     * Expected payload format:
     * {
     *   "subscribe": ["EUR/USD", "USD/TRY"]
     * }
     *
     * Updates the session subscriptions accordingly.
     */
    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
        JsonNode node = mapper.readTree(message.getPayload());
        JsonNode sub = node.get("subscribe");
        if (sub == null || !sub.isArray()) {
            log.warn(
                    "Invalid subscription message format. sessionId={}, payload={}",
                    session.getId(), message.getPayload()
            );
            return;
        }

        Set<String> pairs = new HashSet<>();
        for (JsonNode p : sub) if (p.isString()) pairs.add(p.asString());

        log.info(
                "Subscription updated via WebSocket. sessionId={}, pairs={}",
                session.getId(), pairs
        );
        registry.subscribe(session.getId(), pairs);
    }

    /**
     * Called when a WebSocket connection is closed.
     * Cleans up session and subscription data.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        registry.remove(session.getId());
        log.info(
                "WebSocket connection closed. sessionId={}, status={}",
                session.getId(), status
        );
    }
}

