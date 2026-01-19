package com.emrecelen.rateproducer.monitoring.metrics;

import com.emrecelen.rateproducer.repository.OutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OutboxGauges {

    public OutboxGauges(MeterRegistry registry, OutboxEventRepository outboxRepository) {
        Gauge.builder("outbox.pending.count", outboxRepository, OutboxEventRepository::countPending)
                .description("Number of pending outbox events")
                .register(registry);
    }
}
