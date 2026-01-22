package com.emrecelen.rateproducer.outbox.registry;

import com.emrecelen.rateproducer.outbox.publisher.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registry that maps event types to their corresponding {@link EventPublisher}.
 *
 * <p>
 * This class is initialized at startup and collects all Spring beans
 * implementing {@link EventPublisher}. Each publisher is registered
 * by its {@code eventType()} value.
 * </p>
 *
 * <p>
 * Used by the outbox processing pipeline to dynamically resolve
 * the correct publisher without conditional logic.
 * </p>
 *
 * <p>
 * Example:
 * <pre>
 * RATE_EVENT -> RateRabbitPublisher
 * USER_EVENT -> UserKafkaPublisher
 * </pre>
 * </p>
 */
@Component
public class PublisherRegistry {

    private final Map<String, EventPublisher> publishers;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());


    public PublisherRegistry(List<EventPublisher> list) {
        this.publishers = list.stream()
                .collect(Collectors.toMap(EventPublisher::eventType, p -> p));
        log.info(
                "PublisherRegistry initialized with {} publishers: {}",
                publishers.size(),
                publishers.keySet()
        );
    }

    /**
     * Resolves the publisher for the given event type.
     *
     * @param type logical event type (e.g. RATE_EVENT)
     * @return matching {@link EventPublisher}
     * @throws IllegalStateException if no publisher is registered for the type
     */
    public EventPublisher get(String type) {
        EventPublisher publisher = publishers.get(type);

        if (publisher == null) {
            log.error(
                    "No EventPublisher registered for eventType={}",
                    type
            );
            throw new IllegalStateException(
                    "No EventPublisher registered for type=" + type
            );
        }
        return publisher;
    }
}
