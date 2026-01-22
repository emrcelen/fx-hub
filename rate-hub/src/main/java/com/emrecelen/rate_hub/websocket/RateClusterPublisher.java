package com.emrecelen.rate_hub.websocket;

import com.emrecelen.rate_hub.config.HazelcastConfig;
import com.emrecelen.rate_hub.model.RateView;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RateClusterPublisher {

    private final ITopic<RateView> topic;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());


    /**
     * Publishes rate updates to the Hazelcast cluster topic so that
     * all application instances receive the same rate events.
     * <p>
     * This is used to synchronize in-memory state across multiple
     * running instances (multi-instance WebSocket gateway).
     */
    public RateClusterPublisher(HazelcastInstance hz) {
        this.topic = hz.getTopic(HazelcastConfig.TOPIC_RATE_UPDATES);
    }

    public void publish(RateView view) {
        if (view == null) {
            log.debug("Skipped publishing null RateView to cluster topic");
            return;
        }

        log.debug(
                "Publishing rate update to cluster topic. pair={}, seq={}",
                view.pair(),
                view.seq()
        );

        try {
            topic.publish(view);
            log.trace(
                    "RateView published successfully to Hazelcast topic. pair={}",
                    view.pair()
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to publish rate update to Hazelcast topic. pair={}, error={}",
                    view.pair(),
                    ex.getMessage(),
                    ex
            );
        }
    }
}
