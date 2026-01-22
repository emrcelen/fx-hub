package com.emrecelen.rate_hub.websocket;

import com.emrecelen.rate_hub.config.HazelcastConfig;
import com.emrecelen.rate_hub.model.RateView;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RateClusterListener implements MessageListener<RateView> {

    private final RateBroadcaster broadcaster;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    /**
     * Listens to Hazelcast cluster topic and receives rate updates
     * published by other application instances.
     * <p>
     * This listener ensures that all instances broadcast the same
     * rate updates to their connected WebSocket clients.
     */
    public RateClusterListener(
            HazelcastInstance hz,
            RateBroadcaster broadcaster
    ) {
        this.broadcaster = broadcaster;
        ITopic<RateView> topic = hz.getTopic(HazelcastConfig.TOPIC_RATE_UPDATES);
        topic.addMessageListener(this);
        log.info(
                "RateClusterListener registered to Hazelcast topic={}",
                HazelcastConfig.TOPIC_RATE_UPDATES
        );
    }

    @Override
    public void onMessage(Message<RateView> message) {
        RateView view = message.getMessageObject();

        if (view == null) {
            log.debug("Received null RateView from cluster topic, ignoring");
            return;
        }

        log.debug(
                "Received rate update from cluster. pair={}, seq={}",
                view.pair(),
                view.seq()
        );
        try {
            broadcaster.broadcastView(view);
            log.trace(
                    "RateView successfully broadcasted to WebSocket clients. pair={}",
                    view.pair()
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to broadcast cluster rate update. pair={}, error={}",
                    view.pair(),
                    ex.getMessage(),
                    ex
            );
        }
    }
}
