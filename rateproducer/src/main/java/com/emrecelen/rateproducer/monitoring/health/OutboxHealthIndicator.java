package com.emrecelen.rateproducer.monitoring.health;

import com.emrecelen.rateproducer.repository.OutboxEventRepository;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class OutboxHealthIndicator implements HealthIndicator {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxHealthIndicator(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Override
    public Health health() {
        long pending = outboxEventRepository.countPending();

        if (pending > 10_000) {
            return Health.down().withDetail("outboxPending", pending).build();
        }
        return Health.up().withDetail("outboxPending", pending).build();
    }
}
