package com.emrecelen.rateproducer.outbox.service;

import com.emrecelen.rateproducer.common.Constants;
import com.emrecelen.rateproducer.model.OutboxEvent;
import com.emrecelen.rateproducer.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxClaimService {

    private final OutboxEventRepository repository;

    public OutboxClaimService(OutboxEventRepository repository) {
        this.repository = repository;
    }

    private static final int BATCH_SIZE = 200;

    @Transactional
    public List<OutboxEvent> claimBatch() {
        List<OutboxEvent> retryEvents =
                repository.lockRetryBatch(
                        Constants.OutboxStatus.RETRY,
                        LocalDateTime.now(),
                        PageRequest.of(0, BATCH_SIZE)
                );

        if (!retryEvents.isEmpty()) {
            markProcessing(retryEvents, LocalDateTime.now());
            return retryEvents;
        }

        List<OutboxEvent> newEvents = repository.lockNextBatch(
                List.of(Constants.OutboxStatus.PENDING),
                LocalDateTime.now(),
                PageRequest.of(0, BATCH_SIZE)
        );
        markProcessing(newEvents, LocalDateTime.now());
        return newEvents;
    }

    private void markProcessing(List<OutboxEvent> events, LocalDateTime time) {
        events.forEach(e -> {
            e.setStatus(Constants.OutboxStatus.PROCESSING);
            e.setProcessingStartedAt(time);
        });
        repository.saveAll(events);
    }

}
