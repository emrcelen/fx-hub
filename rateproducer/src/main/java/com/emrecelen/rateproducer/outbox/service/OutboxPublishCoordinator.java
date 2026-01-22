package com.emrecelen.rateproducer.outbox.service;

import com.emrecelen.rateproducer.model.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Coordinates asynchronous publishing of claimed outbox events.
 *
 * <p>
 * This component dispatches each {@link OutboxEvent} to a virtual thread
 * for parallel processing via {@link OutboxEventProcessor}.
 * </p>
 *
 * <p>
 * Virtual threads are used to:
 * <ul>
 *     <li>Keep publishing fully non-blocking</li>
 *     <li>Avoid thread pool exhaustion under high load</li>
 *     <li>Scale efficiently with large event batches</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class is stateless and safe to be used concurrently
 * by multiple scheduler invocations and application instances.
 * </p>
 */
@Service
public class OutboxPublishCoordinator {
    private final OutboxEventProcessor processor;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public OutboxPublishCoordinator(OutboxEventProcessor processor) {
        this.processor = processor;
    }

    public void publishAsync(List<OutboxEvent> events) {
        if (events == null || events.isEmpty()) {
            log.debug("Publish coordinator invoked with empty event list.");
            return;
        }

        log.info("Dispatching {} outbox events for async publishing", events.size());

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            events.forEach(e -> executor.submit(() -> processor.process(e)));
        }
    }
}
