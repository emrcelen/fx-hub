package com.emrecelen.rateproducer.outbox.publisher;

import com.emrecelen.rateproducer.model.OutboxEvent;

public interface EventPublisher {
    String eventType();

    void publish(OutboxEvent event);
}
