package com.emrecelen.rateproducer.outbox;

import com.emrecelen.rateproducer.common.Constants;
import com.emrecelen.rateproducer.model.OutboxEvent;
import com.emrecelen.rateproducer.outbox.publisher.EventPublisher;
import com.emrecelen.rateproducer.outbox.registry.PublisherRegistry;
import com.emrecelen.rateproducer.outbox.service.OutboxEventProcessor;
import com.emrecelen.rateproducer.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTest {

    @Mock
    private PublisherRegistry registry;

    @Mock
    private EventPublisher publisher;

    @Mock
    private OutboxEventRepository repository;

    private OutboxEventProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OutboxEventProcessor(registry, repository);
    }

    @Test
    void should_mark_event_as_sent_when_publish_succeeds() {
        OutboxEvent event = OutboxEvent.pending(
                "anyEventKey",
                Constants.OutboxType.RATE_EVENT,
                1,
                null
        );
        event.setAttempts(0);
        when(registry.get(Constants.OutboxType.RATE_EVENT.name()))
                .thenReturn(publisher);

        processor.process(event);

        verify(publisher).publish(event);
        assertThat(event.getStatus()).isEqualTo(Constants.OutboxStatus.SENT);
        assertThat(event.getLastError()).isNull();
        assertThat(event.getAttempts()).isEqualTo(0);
        verify(repository).save(event);
    }

    @Test
    void should_retry_when_publish_fails_and_attempts_below_max() {
        OutboxEvent event = OutboxEvent.pending(
                "anyEventKey",
                Constants.OutboxType.RATE_EVENT,
                1,
                null
        );
        event.setAttempts(2);

        when(registry.get(Constants.OutboxType.RATE_EVENT.name()))
                .thenReturn(publisher);

        doThrow(new RuntimeException("broker down"))
                .when(publisher).publish(event);

        processor.process(event);
        assertThat(event.getAttempts()).isEqualTo(3);
        assertThat(event.getStatus()).isEqualTo(Constants.OutboxStatus.RETRY);
        assertThat(event.getLastError()).isEqualTo("broker down");
        assertThat(event.getAvailableAt()).isNotNull();
        verify(repository).save(event);
    }

    @Test
    void should_mark_event_as_failed_when_max_attempts_reached() {
        // given
        OutboxEvent event = OutboxEvent.pending(
                "anyEventKey",
                Constants.OutboxType.RATE_EVENT,
                1,
                null
        );
        event.setAttempts(4);

        when(registry.get(Constants.OutboxType.RATE_EVENT.name()))
                .thenReturn(publisher);

        doThrow(new RuntimeException("permanent failure"))
                .when(publisher).publish(event);

        processor.process(event);

        assertThat(event.getAttempts()).isEqualTo(5);
        assertThat(event.getStatus()).isEqualTo(Constants.OutboxStatus.FAILED);
        assertThat(event.getAvailableAt()).isNull();
        assertThat(event.getLastError()).isEqualTo("permanent failure");

        verify(repository).save(event);
    }

    @Test
    void should_fail_and_retry_when_publisher_not_found() {
        // given
        OutboxEvent event = OutboxEvent.pending(
                "anyEventKey",
                Constants.OutboxType.RATE_EVENT,
                1,
                null
        );
        event.setAttempts(0);

        when(registry.get(anyString()))
                .thenThrow(new IllegalStateException("No publisher"));

        // when
        processor.process(event);

        // then
        assertThat(event.getAttempts()).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo(Constants.OutboxStatus.RETRY);
        assertThat(event.getLastError()).isEqualTo("No publisher");

        verify(repository).save(event);
    }

}
