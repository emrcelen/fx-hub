package com.emrecelen.rate_hub.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    /**
     * Name of the running application instance.
     * Used to inform the client which node it is connected to
     * in a multi-instance WebSocket deployment.
     */
    @Value("${spring.application.instance-name}")
    private String instanceName;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> subs = new ConcurrentHashMap<>();

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    /**
     * Registers a new WebSocket session.
     * Sends a handshake message to the client with instance information.
     */
    public void add(WebSocketSession s) {
        sessions.put(s.getId(), s);
        log.info("WebSocket session registered. sessionId={}", s.getId());
        try {
            if (s.isOpen()) {
                String payload = "Connection Instance: ".concat(instanceName);
                s.sendMessage(new TextMessage(payload));
                log.debug("Handshake message sent. sessionId={}", s.getId());
            }
        } catch (Exception ex) {
            log.warn(
                    "Failed to send handshake message. sessionId={}, error={}",
                    s.getId(), ex.getMessage()
            );
        }
    }

    /**
     * Removes a WebSocket session and clears all subscriptions.
     */
    public void remove(String id) {
        sessions.remove(id);
        subs.remove(id);
        log.info("WebSocket session removed. sessionId={}", id);
    }

    /**
     * Updates the subscription list for a session.
     */
    public void subscribe(String id, Set<String> pairs) {
        subs.put(id, Set.copyOf(pairs));
        log.info(
                "Session subscription updated. sessionId={}, pairs={}",
                id, pairs
        );
    }

    /**
     * Returns all open WebSocket sessions that are interested in the given pair.
     * Used before broadcasting rate updates.
     */
    public Collection<WebSocketSession> interestedIn(String pair) {
        List<WebSocketSession> list = new ArrayList<>();
        for (WebSocketSession s : sessions.values()) {
            if (!s.isOpen()) continue;
            if (subs.getOrDefault(s.getId(), Set.of()).contains(pair)) list.add(s);
        }
        log.debug(
                "Broadcast target lookup completed. pair={}, sessionCount={}",
                pair, list.size()
        );
        return list;
    }
}

