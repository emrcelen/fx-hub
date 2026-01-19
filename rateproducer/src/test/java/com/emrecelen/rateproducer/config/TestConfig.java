package com.emrecelen.rateproducer.config;

import com.emrecelen.rateproducer.monitoring.metrics.RateProducerMetrics;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {
    @Bean
    RateProducerMetrics metrics() {
        return mock(RateProducerMetrics.class);
    }
}

