package com.emrecelen.rateproducer.outbox.service;

import com.emrecelen.rateproducer.common.Constants;
import com.emrecelen.rateproducer.model.OutboxEvent;
import com.emrecelen.rateproducer.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for claiming outbox events for publishing.
 *
 * <p>
 * This service selects a batch of events that are ready to be processed,
 * applies row-level locking to prevent multiple instances from processing
 * the same events, and marks them as {@code PROCESSING}.
 * </p>
 *
 * <p>
 * Claiming strategy:
 * <ul>
 *     <li>Retry events are always prioritized</li>
 *     <li>If no retry events exist, pending events are claimed</li>
 * </ul>
 * </p>
 *
 * <p>
 * This design allows safe multi-instance execution and guarantees
 * at-least-once delivery semantics for the outbox pattern.
 * </p>
 */
@Service
public class OutboxClaimService {

    private final OutboxEventRepository repository;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public OutboxClaimService(OutboxEventRepository repository) {
        this.repository = repository;
    }

    private static final int BATCH_SIZE = 200;

    /**
     * Claims the next batch of outbox events to be processed.
     *
     * <p>
     * Retry events are always claimed first. If no retry events are available,
     * a batch of new pending events is claimed instead.
     * </p>
     *
     * @return list of claimed events, possibly empty
     */
    @Transactional
    public List<OutboxEvent> claimBatch() {
        List<OutboxEvent> retryEvents =
                repository.lockRetryBatch(
                        Constants.OutboxStatus.RETRY,
                        LocalDateTime.now(),
                        PageRequest.of(0, BATCH_SIZE)
                );

        if (!retryEvents.isEmpty()) {
            log.debug(
                    "Claimed {} RETRY outbox events for processing",
                    retryEvents.size()
            );
            markProcessing(retryEvents, LocalDateTime.now());
            return retryEvents;
        }

        List<OutboxEvent> newEvents = repository.lockNextBatch(
                List.of(Constants.OutboxStatus.PENDING),
                LocalDateTime.now(),
                PageRequest.of(0, BATCH_SIZE)
        );
        if (!newEvents.isEmpty()) {
            log.debug(
                    "Claimed {} PENDING outbox events for processing",
                    newEvents.size()
            );
        }
        markProcessing(newEvents, LocalDateTime.now());
        return newEvents;
    }

    /**
     * Marks claimed events as {@code PROCESSING} and persists the change.
     *
     * <p>
     * This timestamp is later used by the watchdog to detect stuck events.
     * </p>
     */
    private void markProcessing(List<OutboxEvent> events, LocalDateTime time) {
        events.forEach(e -> {
            e.setStatus(Constants.OutboxStatus.PROCESSING);
            e.setProcessingStartedAt(time);
        });
        repository.saveAll(events);
    }

}
