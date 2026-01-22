package com.emrecelen.rateproducer.service;

import com.emrecelen.rateproducer.common.Constants;
import com.emrecelen.rateproducer.common.JsonUtil;
import com.emrecelen.rateproducer.domain.registry.EventFactoryRegistry;
import com.emrecelen.rateproducer.domain.service.DomainEventFactory;
import com.emrecelen.rateproducer.model.OutboxEvent;
import com.emrecelen.rateproducer.monitoring.metrics.RateProducerMetrics;
import com.emrecelen.rateproducer.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class OutboxWriter {

    private final OutboxEventRepository repository;
    private final EventFactoryRegistry registry;
    private final RateProducerMetrics rateProducerMetrics;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public OutboxWriter(
            OutboxEventRepository repository,
            EventFactoryRegistry registry,
            RateProducerMetrics rateProducerMetrics
    ) {
        this.repository = repository;
        this.registry = registry;
        this.rateProducerMetrics = rateProducerMetrics;
    }

    /**
     * Persists a domain event into the outbox table in a transactional manner.
     *
     * <p>
     * This method is the single entry point for producing events that will be
     * published asynchronously. It guarantees:
     * </p>
     *
     * <ul>
     *   <li>Transactional consistency with the business operation</li>
     *   <li>Idempotency via unique event key constraint</li>
     *   <li>Safe retry by outbox poller</li>
     * </ul>
     *
     * @param eventType logical event type (e.g. RATE_EVENT)
     * @param input     domain object used to build the event
     * @param <T>       domain input type
     */
    @Transactional
    public <T> void write(String eventType, T input) {
        log.debug(
                "Writing outbox event. eventType={} inputType={}",
                eventType,
                input.getClass().getSimpleName()
        );
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
            log.info(
                    "Outbox event persisted successfully. eventKey={} schemaVersion={}",
                    entity.getEventKey(),
                    entity.getSchemaVersion()
            );
        } catch (DataIntegrityViolationException ex) {
            // idempotent duplicate
            log.warn(
                    "Duplicate outbox event detected (idempotent). eventKey={}",
                    entity.getEventKey()
            );
            rateProducerMetrics.publishFailed();
        }
    }
}
