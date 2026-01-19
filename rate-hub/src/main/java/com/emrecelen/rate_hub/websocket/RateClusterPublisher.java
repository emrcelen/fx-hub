package com.emrecelen.rate_hub.websocket;

import com.emrecelen.rate_hub.config.HazelcastConfig;
import com.emrecelen.rate_hub.model.RateView;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import org.springframework.stereotype.Component;

@Component
public class RateClusterPublisher {

    private final ITopic<RateView> topic;

    public RateClusterPublisher(HazelcastInstance hz) {
        this.topic = hz.getTopic(HazelcastConfig.TOPIC_RATE_UPDATES);
    }

    public void publish(RateView view) {
        if (view != null) topic.publish(view);
    }
}
