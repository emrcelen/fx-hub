package com.emrecelen.rateproducer.outbox;

import com.emrecelen.rateproducer.common.Constants;
import com.emrecelen.rateproducer.model.OutboxEvent;
import com.emrecelen.rateproducer.outbox.service.OutboxClaimService;
import com.emrecelen.rateproducer.outbox.service.OutboxPoller;
import com.emrecelen.rateproducer.outbox.service.OutboxPublishCoordinator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPollerTest {

    @Mock
    private OutboxClaimService claimService;

    @Mock
    private OutboxPublishCoordinator coordinator;

    private OutboxPoller poller;

    @BeforeEach
    void setUp() {
        poller = new OutboxPoller(claimService, coordinator);
    }

    @Test
    void should_claim_and_publish_events() {
        List<OutboxEvent> events = List.of(
                OutboxEvent.pending(
                        "anyEventKey1",
                        Constants.OutboxType.RATE_EVENT,
                        1,
                        null
                ),
                OutboxEvent.pending(
                        "anyEventKey2",
                        Constants.OutboxType.RATE_EVENT,
                        1,
                        null
                )
        );
        when(claimService.claimBatch()).thenReturn(events);
        poller.poll();
        verify(claimService).claimBatch();
        verify(coordinator).publishAsync(events);
    }


    @Test
    void should_publish_empty_list_when_no_events() {
        when(claimService.claimBatch()).thenReturn(List.of());

        poller.poll();

        verify(claimService).claimBatch();
        verify(coordinator).publishAsync(List.of());
    }


    @Test
    void should_propagate_exception_when_claim_fails() {
        when(claimService.claimBatch())
                .thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> poller.poll())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");

        verify(coordinator, never()).publishAsync(any());
    }

}
