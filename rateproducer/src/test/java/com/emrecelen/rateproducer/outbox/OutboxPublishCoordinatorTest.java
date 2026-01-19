package com.emrecelen.rateproducer.outbox;

import com.emrecelen.rateproducer.common.Constants;
import com.emrecelen.rateproducer.model.OutboxEvent;
import com.emrecelen.rateproducer.outbox.service.OutboxEventProcessor;
import com.emrecelen.rateproducer.outbox.service.OutboxPublishCoordinator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OutboxPublishCoordinatorTest {

    @Mock
    private OutboxEventProcessor processor;

    private OutboxPublishCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator = new OutboxPublishCoordinator(processor);
    }


    @Test
    void should_process_all_events_async() throws Exception {
        // given
        OutboxEvent e1 = OutboxEvent.pending(
                "anyEventKey1",
                Constants.OutboxType.RATE_EVENT,
                1,
                null
        );
        OutboxEvent e2 = OutboxEvent.pending(
                "anyEventKey2",
                Constants.OutboxType.RATE_EVENT,
                1,
                null
        );
        OutboxEvent e3 = OutboxEvent.pending(
                "anyEventKey3",
                Constants.OutboxType.RATE_EVENT,
                1,
                null
        );

        List<OutboxEvent> events = List.of(e1, e2, e3);

        coordinator.publishAsync(events);

        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(processor).process(e1);
                    verify(processor).process(e2);
                    verify(processor).process(e3);
                });
    }

    @Test
    void should_not_call_processor_when_no_events() {
        coordinator.publishAsync(List.of());

        verifyNoInteractions(processor);
    }

}
