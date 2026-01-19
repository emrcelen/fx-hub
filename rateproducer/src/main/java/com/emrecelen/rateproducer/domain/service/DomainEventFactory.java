package com.emrecelen.rateproducer.domain.service;

public interface DomainEventFactory<T> {
    String eventType();

    int schemaVersion();

    String eventKey(T input);

    Object createEvent(T input);
}
