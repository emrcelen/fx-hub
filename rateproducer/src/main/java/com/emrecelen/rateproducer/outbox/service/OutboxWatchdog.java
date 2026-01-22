package com.emrecelen.rateproducer.outbox.service;

import com.emrecelen.rateproducer.common.Constants;
import com.emrecelen.rateproducer.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Periodically scans and reclaims stuck outbox events.
 *
 * <p>
 * This watchdog is responsible for detecting outbox events that were marked
 * as {@code PROCESSING} but never completed due to application crashes,
 * node restarts, or unexpected failures.
 * </p>
 *
 * <p>
 * If an event stays in processing state longer than {@link #PROCESSING_TTL},
 * it is considered stuck and moved back to {@code RETRY} state so it can be
 * picked up again by the poller.
 * </p>
 *
 * <p>
 * This mechanism guarantees at-least-once delivery and prevents silent
 * message loss in distributed environments.
 * </p>
 */
@Component
public class OutboxWatchdog {

    private static final Duration PROCESSING_TTL = Duration.ofSeconds(30);

    private final OutboxEventRepository repository;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public OutboxWatchdog(OutboxEventRepository repository) {
        this.repository = repository;
    }


    @Transactional
    @Scheduled(fixedDelayString = "${outbox.watchdog.delay-ms:10000}")
    public void reclaim() {
        LocalDateTime threshold = LocalDateTime.now().minus(PROCESSING_TTL);
        int count = repository.reclaimStuck(Constants.OutboxStatus.RETRY, threshold);

        if (count > 0) {
            log.warn(
                    "Reclaimed {} stuck outbox events older than {} seconds",
                    count,
                    PROCESSING_TTL.getSeconds()
            );
        } else {
            log.debug("No stuck outbox events found to reclaim");
        }
    }
}
