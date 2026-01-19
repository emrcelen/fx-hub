package com.emrecelen.rate_hub.websocket;

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

    @Value("${spring.application.instance-name}")
    private String instanceName;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> subs = new ConcurrentHashMap<>();

    public void add(WebSocketSession s) {
        sessions.put(s.getId(), s);
        try {
            if (s.isOpen()) {
                String payload = "Connection Instance: ".concat(instanceName);
                s.sendMessage(new TextMessage(payload));
            }
        } catch (Exception ignore) {
        }
    }

    public void remove(String id) {
        sessions.remove(id);
        subs.remove(id);
    }

    public void subscribe(String id, Set<String> pairs) {
        subs.put(id, Set.copyOf(pairs));
    }

    public Collection<WebSocketSession> interestedIn(String pair) {
        List<WebSocketSession> list = new ArrayList<>();
        for (WebSocketSession s : sessions.values()) {
            if (!s.isOpen()) continue;
            if (subs.getOrDefault(s.getId(), Set.of()).contains(pair)) list.add(s);
        }
        return list;
    }
}

