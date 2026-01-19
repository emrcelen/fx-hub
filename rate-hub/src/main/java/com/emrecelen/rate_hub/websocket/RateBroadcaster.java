package com.emrecelen.rate_hub.websocket;

import com.emrecelen.rate_hub.model.RateView;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class RateBroadcaster {

    private final SessionRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();

    private final ExecutorService wsExecutor =
            Executors.newVirtualThreadPerTaskExecutor();

    public RateBroadcaster(SessionRegistry registry) {
        this.registry = registry;
    }

    public void broadcastView(RateView view) {
        if (view == null || view.pair() == null) return;

        String payload;
        try {
            payload = mapper.writeValueAsString(view);
        } catch (Exception ignore) {
            return;
        }

        for (WebSocketSession s : registry.interestedIn(view.pair())) {
            wsExecutor.submit(() -> {
                try {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(payload));
                    }
                } catch (Exception ignore) {
                }
            });
        }
    }

    @PreDestroy
    void shutdown() {
        wsExecutor.shutdown();
    }
}
