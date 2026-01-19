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

@Component
public class OutboxWatchdog {

    private static final Duration PROCESSING_TTL = Duration.ofSeconds(30);

    private final OutboxEventRepository repository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public OutboxWatchdog(OutboxEventRepository repository) {
        this.repository = repository;
    }


    @Transactional
    @Scheduled(fixedDelayString = "${outbox.watchdog.delay-ms:10000}")
    public void reclaim() {
        LocalDateTime threshold = LocalDateTime.now().minus(PROCESSING_TTL);
        int count = repository.reclaimStuck(Constants.OutboxStatus.RETRY, threshold);

        if (count > 0) {
            logger.warn("Reclaimed {} stuck outbox events", count);
        }
    }
}
