package com.emrecelen.rate_hub.websocket;

import com.emrecelen.rate_hub.config.HazelcastConfig;
import com.emrecelen.rate_hub.model.RateView;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class RateClusterListener implements MessageListener<RateView> {

    private final RateBroadcaster broadcaster;

    public RateClusterListener(
            HazelcastInstance hz,
            RateBroadcaster broadcaster
    ) {
        this.broadcaster = broadcaster;
        ITopic<RateView> topic = hz.getTopic(HazelcastConfig.TOPIC_RATE_UPDATES);
        topic.addMessageListener(this);
    }

    @Override
    public void onMessage(Message<RateView> message) {
        broadcaster.broadcastView(message.getMessageObject());
    }
}
