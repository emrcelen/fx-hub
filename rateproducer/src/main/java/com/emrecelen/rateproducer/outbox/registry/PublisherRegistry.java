package com.emrecelen.rateproducer.outbox.registry;

import com.emrecelen.rateproducer.outbox.publisher.EventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PublisherRegistry {

    private final Map<String, EventPublisher> publishers;

    public PublisherRegistry(List<EventPublisher> list) {
        this.publishers = list.stream()
                .collect(Collectors.toMap(EventPublisher::eventType, p -> p));
    }

    public EventPublisher get(String type) {
        return publishers.get(type);
    }
}
