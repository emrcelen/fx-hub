package com.emrecelen.rateproducer.service;

import com.emrecelen.rateproducer.common.Constants;
import com.emrecelen.rateproducer.common.JsonUtil;
import com.emrecelen.rateproducer.domain.registry.EventFactoryRegistry;
import com.emrecelen.rateproducer.domain.service.DomainEventFactory;
import com.emrecelen.rateproducer.model.OutboxEvent;
import com.emrecelen.rateproducer.monitoring.metrics.RateProducerMetrics;
import com.emrecelen.rateproducer.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class OutboxWriter {

    private final OutboxEventRepository repository;
    private final EventFactoryRegistry registry;
    private final RateProducerMetrics rateProducerMetrics;

    public OutboxWriter(
            OutboxEventRepository repository,
            EventFactoryRegistry registry,
            RateProducerMetrics rateProducerMetrics
    ) {
        this.repository = repository;
        this.registry = registry;
        this.rateProducerMetrics = rateProducerMetrics;
    }

    @Transactional
    public <T> void write(String eventType, T input) {
        DomainEventFactory<T> factory = registry.get(eventType);
        Object event = factory.createEvent(input);
        OutboxEvent entity = OutboxEvent.pending(
                factory.eventKey(input),
                Constants.OutboxType.RATE_EVENT,
                factory.schemaVersion(),
                JsonUtil.toJson(event)
        );

        try {
            repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException ex) {
            // idempotent duplicate
            rateProducerMetrics.publishFailed();
        }
    }
}
