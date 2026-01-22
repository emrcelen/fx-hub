package com.emrecelen.rateproducer.outbox.service;

import com.emrecelen.rateproducer.model.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Periodically polls the outbox table and dispatches events for publishing.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Claiming publishable outbox events in batches</li>
 *     <li>Preventing duplicate processing across instances</li>
 *     <li>Delegating async publish to {@link OutboxPublishCoordinator}</li>
 * </ul>
 * </p>
 *
 * <p>
 * This component is designed to run safely in a multi-instance environment.
 * Each poll operation claims events using database-level locking to ensure
 * only one instance processes a given event.
 * </p>
 *
 * <p>
 * Poll interval is configurable via:
 * <pre>
 * outbox.poll.delay-ms (default: 200ms)
 * </pre>
 * </p>
 */
@Component
public class OutboxPoller {

    private final OutboxClaimService claimService;
    private final OutboxPublishCoordinator coordinator;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public OutboxPoller(
            OutboxClaimService claimService,
            OutboxPublishCoordinator coordinator
    ) {
        this.claimService = claimService;
        this.coordinator = coordinator;
    }

    @Scheduled(fixedDelayString = "${outbox.poll.delay-ms:200}")
    public void poll() {
        List<OutboxEvent> claimed = claimService.claimBatch();
        if (claimed.isEmpty()) {
            log.debug("Outbox poll executed. No events claimed.");
            return;
        }

        log.info(
                "Outbox poll claimed {} events. Dispatching for publish.",
                claimed.size()
        );
        coordinator.publishAsync(claimed);
    }
}
