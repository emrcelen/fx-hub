package com.emrecelen.rateproducer.outbox.service;

import com.emrecelen.rateproducer.model.OutboxEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPoller {

    private final OutboxClaimService claimService;
    private final OutboxPublishCoordinator coordinator;

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
        coordinator.publishAsync(claimed);
    }
}
