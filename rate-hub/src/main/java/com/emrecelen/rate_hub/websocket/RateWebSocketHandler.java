package com.emrecelen.rate_hub.websocket;

import org.jspecify.annotations.NonNull;
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

    public RateWebSocketHandler(SessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        registry.add(session);
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
        JsonNode node = mapper.readTree(message.getPayload());
        JsonNode sub = node.get("subscribe");
        if (sub == null || !sub.isArray()) return;

        Set<String> pairs = new HashSet<>();
        for (JsonNode p : sub) if (p.isString()) pairs.add(p.asString());

        registry.subscribe(session.getId(), pairs);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        registry.remove(session.getId());
    }
}

