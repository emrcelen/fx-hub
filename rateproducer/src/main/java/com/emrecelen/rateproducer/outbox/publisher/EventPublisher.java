package com.emrecelen.rateproducer.outbox.publisher;

import com.emrecelen.rateproducer.model.OutboxEvent;

/**
 * Contract for publishing outbox events to external systems.
 *
 * <p>
 * Implementations of this interface are responsible for delivering
 * persisted outbox events to their final transport (e.g. RabbitMQ,
 * Kafka, HTTP, etc.).
 * </p>
 *
 * <p>
 * This interface is intentionally transport-agnostic and is used by
 * {@link com.emrecelen.rateproducer.outbox.service.OutboxEventProcessor}
 * to dynamically route events based on {@code eventType}.
 * </p>
 *
 * <p>
 * Implementations must be:
 * </p>
 * <ul>
 *   <li>Idempotent</li>
 *   <li>Fast (non-blocking if possible)</li>
 *   <li>Fail-safe (exceptions trigger retry logic)</li>
 * </ul>
 */
public interface EventPublisher {
    String eventType();

    void publish(OutboxEvent event);
}
