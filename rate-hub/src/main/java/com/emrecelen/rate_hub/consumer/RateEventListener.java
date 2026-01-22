package com.emrecelen.rate_hub.consumer;

import com.emrecelen.rate_hub.model.StoreUpdate;
import com.emrecelen.rate_hub.service.RateStoreService;
import com.emrecelen.rate_hub.websocket.RateClusterPublisher;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class RateEventListener {

    private final RateStoreService store;
    private final RateClusterPublisher clusterPublisher;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public RateEventListener(RateStoreService store, RateClusterPublisher clusterPublisher) {
        this.store = store;
        this.clusterPublisher = clusterPublisher;
    }

    @RabbitListener(queues = "rate.input.queue")
    public void consume(Message message, Channel channel) throws Exception {
        long tag = message.getMessageProperties().getDeliveryTag();
        String raw = new String(message.getBody(), StandardCharsets.UTF_8);

        log.debug(
                "Rate message received. deliveryTag={}, size={} bytes",
                tag,
                message.getBody().length
        );

        try {
            StoreUpdate update = store.onRawMessage(raw);
            channel.basicAck(tag, false);
            if (update.view() != null) {
                log.debug(
                        "Rate accepted and stored. pair={}, seq={}",
                        update.view().pair(),
                        update.view().seq()
                );
                clusterPublisher.publish(update.view());
            }
        } catch (Exception ex) {
            log.error(
                    "Failed to process rate message. Message dropped. deliveryTag={}",
                    tag,
                    ex
            );
            channel.basicAck(tag, false);
        }
    }
}
