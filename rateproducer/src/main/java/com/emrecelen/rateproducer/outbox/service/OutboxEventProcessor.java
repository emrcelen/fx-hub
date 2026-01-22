package com.emrecelen.rateproducer.outbox.service;

import com.emrecelen.rateproducer.model.OutboxEvent;
import com.emrecelen.rateproducer.common.Constants.OutboxStatus;
import com.emrecelen.rateproducer.outbox.registry.PublisherRegistry;
import com.emrecelen.rateproducer.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Processes a single outbox event and publishes it using the appropriate publisher.
 *
 * <p>
 * This service is responsible for:
 * <ul>
 *     <li>Executing the publish operation</li>
 *     <li>Handling retry & backoff logic on failures</li>
 *     <li>Updating event status atomically</li>
 * </ul>
 * </p>
 *
 * <p>
 * Retry strategy:
 * <ul>
 *     <li>Exponential backoff (capped at 30s)</li>
 *     <li>Max attempts = 5</li>
 *     <li>After max attempts, event is marked as FAILED</li>
 * </ul>
 * </p>
 *
 * <p>
 * This method is transactional to guarantee state consistency between
 * publish attempts and database updates.
 * </p>
 */
@Service
public class OutboxEventProcessor {
    private final PublisherRegistry registry;
    private final OutboxEventRepository repository;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public OutboxEventProcessor(PublisherRegistry registry,
                                OutboxEventRepository repository) {
        this.registry = registry;
        this.repository = repository;
    }

    private static final int MAX_ATTEMPTS = 5;

    @Transactional
    public void process(OutboxEvent e) {
        try {
            log.debug(
                    "Publishing outbox event. id={}, type={}, attempt={}",
                    e.getId(),
                    e.getEventType(),
                    e.getAttempts()
            );
            registry.get(e.getEventType().name()).publish(e);

            e.setStatus(OutboxStatus.SENT);
            e.setLastError(null);

            log.info(
                    "Outbox event published successfully. id={}, type={}",
                    e.getId(),
                    e.getEventType()
            );

        } catch (Exception ex) {
            int nextAttempt = e.getAttempts() + 1;
            e.setAttempts(nextAttempt);
            e.setLastError(ex.getMessage());

            if (nextAttempt >= MAX_ATTEMPTS) {
                e.setStatus(OutboxStatus.FAILED);
                e.setAvailableAt(null);

                log.error(
                        "Outbox event permanently failed after {} attempts. id={}, type={}, error={}",
                        nextAttempt,
                        e.getId(),
                        e.getEventType(),
                        ex.getMessage(),
                        ex
                );
            } else {
                LocalDateTime next = nextAvailableAt(nextAttempt);
                e.setStatus(OutboxStatus.RETRY);
                e.setAvailableAt(next);
                log.warn(
                        "Outbox event publish failed. Retrying. id={}, type={}, attempt={}, nextAt={}",
                        e.getId(),
                        e.getEventType(),
                        nextAttempt,
                        next
                );
            }
        }
        repository.save(e);
    }

    /**
     * Calculates next retry time using exponential backoff.
     *
     * <p>
     * Delay formula:
     * <pre>
     * delay = min(250ms * 2^attempt, 30s)
     * </pre>
     * </p>
     */
    private LocalDateTime nextAvailableAt(int attempts) {
        long delayMs = Math.min(250L * (1L << Math.min(attempts, 6)), 30_000L);
        return LocalDateTime.now().plus(Duration.ofMillis(delayMs));
    }
}
