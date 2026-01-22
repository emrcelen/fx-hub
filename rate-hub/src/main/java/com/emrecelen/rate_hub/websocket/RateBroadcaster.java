package com.emrecelen.rate_hub.websocket;

import com.emrecelen.rate_hub.model.RateView;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class RateBroadcaster {

    private final SessionRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();

    private final ExecutorService wsExecutor =
            Executors.newVirtualThreadPerTaskExecutor();

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    /**
     * Broadcasts rate updates to all WebSocket sessions
     * that are subscribed to the given currency pair.
     */
    public RateBroadcaster(SessionRegistry registry) {
        this.registry = registry;
    }


    /**
     * Sends the given {@link RateView} to all interested WebSocket clients.
     *
     * This method is intentionally non-blocking:
     * each session send operation is executed in a virtual thread
     * to prevent slow clients from blocking the main flow.
     *
     * @param view the rate snapshot to broadcast
     */
    public void broadcastView(RateView view) {
        if (view == null || view.pair() == null) {
            log.debug("RateView is null or missing pair, skipping broadcast");
            return;
        }

        String payload;
        try {
            payload = mapper.writeValueAsString(view);
        } catch (Exception ex) {
            log.error(
                    "Failed to serialize RateView for broadcast. pair={}, error={}",
                    view.pair(),
                    ex.getMessage(),
                    ex
            );
            return;
        }

        Collection<WebSocketSession> targets =
                registry.interestedIn(view.pair());

        if (targets.isEmpty()) {
            log.trace(
                    "No active subscribers for pair={}, broadcast skipped",
                    view.pair()
            );
            return;
        }

        log.debug(
                "Broadcasting rate update to {} session(s). pair={}, seq={}",
                targets.size(),
                view.pair(),
                view.seq()
        );

        for (WebSocketSession s : registry.interestedIn(view.pair())) {
            wsExecutor.submit(() -> {
                try {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(payload));
                    }
                } catch (Exception ex) {
                    log.warn(
                            "Failed to send WebSocket message. sessionId={}, pair={}, error={}",
                            s.getId(),
                            view.pair(),
                            ex.getMessage()
                    );
                }
            });
        }
    }

    @PreDestroy
    void shutdown() {
        wsExecutor.shutdown();
    }
}
