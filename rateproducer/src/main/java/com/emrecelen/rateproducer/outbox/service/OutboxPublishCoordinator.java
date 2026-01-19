package com.emrecelen.rateproducer.outbox.service;

import com.emrecelen.rateproducer.model.OutboxEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class OutboxPublishCoordinator {
    private final OutboxEventProcessor processor;

    public OutboxPublishCoordinator(OutboxEventProcessor processor) {
        this.processor = processor;
    }

    public void publishAsync(List<OutboxEvent> events) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            events.forEach(e -> executor.submit(() -> processor.process(e)));
        }
    }
}
