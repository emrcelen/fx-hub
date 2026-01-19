package com.emrecelen.rateproducer.outbox.service;

import com.emrecelen.rateproducer.model.OutboxEvent;
import com.emrecelen.rateproducer.common.Constants.OutboxStatus;
import com.emrecelen.rateproducer.outbox.registry.PublisherRegistry;
import com.emrecelen.rateproducer.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class OutboxEventProcessor {
    private final PublisherRegistry registry;
    private final OutboxEventRepository repository;

    public OutboxEventProcessor(PublisherRegistry registry,
                                OutboxEventRepository repository) {
        this.registry = registry;
        this.repository = repository;
    }

    private static final int MAX_ATTEMPTS = 5;

    @Transactional
    public void process(OutboxEvent e) {
        try {
            registry.get(e.getEventType().name()).publish(e);

            e.setStatus(OutboxStatus.SENT);
            e.setLastError(null);

        } catch (Exception ex) {
            int nextAttempt = e.getAttempts() + 1;
            e.setAttempts(nextAttempt);
            e.setLastError(ex.getMessage());

            if (nextAttempt >= MAX_ATTEMPTS) {
                e.setStatus(OutboxStatus.FAILED);
                e.setAvailableAt(null);
            } else {
                e.setStatus(OutboxStatus.RETRY);
                e.setAvailableAt(nextAvailableAt(nextAttempt));
            }
        }
        repository.save(e);
    }

    private LocalDateTime nextAvailableAt(int attempts) {
        long delayMs = Math.min(250L * (1L << Math.min(attempts, 6)), 30_000L);
        return LocalDateTime.now().plus(Duration.ofMillis(delayMs));
    }
}
