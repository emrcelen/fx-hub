package com.emrecelen.rateproducer.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class RateProducerMetrics {

    private final Counter ingestReceived;
    private final Counter ingestRejected;
    private final Counter publishFailed;
    private final Timer ingestLatency;

    public RateProducerMetrics(MeterRegistry registry) {
        this.ingestReceived = registry.counter("rate.ingest.received");
        this.ingestRejected = registry.counter("rate.ingest.rejected");
        this.publishFailed  = registry.counter("rate.publish.failed");
        this.ingestLatency  = registry.timer("rate.ingest.latency");
    }

    public void received() { ingestReceived.increment(); }

    public void rejected() { ingestRejected.increment(); }

    public void publishFailed() { publishFailed.increment(); }

    public <T> T recordLatency(Supplier<T> supplier) {
        return ingestLatency.record(supplier);
    }

    public void recordLatency(Runnable runnable) {
        ingestLatency.record(runnable);
    }
}
