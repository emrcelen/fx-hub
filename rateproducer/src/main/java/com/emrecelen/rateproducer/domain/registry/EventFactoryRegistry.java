package com.emrecelen.rateproducer.domain.registry;

import com.emrecelen.rateproducer.domain.service.DomainEventFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EventFactoryRegistry {
    private final Map<String, DomainEventFactory<?>> factories;

    public EventFactoryRegistry(List<DomainEventFactory<?>> list) {
        this.factories = list.stream()
                .collect(Collectors.toMap(DomainEventFactory::eventType, f -> f));
    }

    public <T> DomainEventFactory<T> get(String type) {
        return (DomainEventFactory<T>) factories.get(type);
    }
}
