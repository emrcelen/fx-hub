package com.emrecelen.rateproducer.outbox;

import com.emrecelen.rateproducer.common.Constants;
import com.emrecelen.rateproducer.outbox.service.OutboxWatchdog;
import com.emrecelen.rateproducer.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxWatchdogTest {

    @Mock
    private OutboxEventRepository repository;

    private OutboxWatchdog watchdog;

    @BeforeEach
    void setUp() {
        watchdog = new OutboxWatchdog(repository);
    }

    @Test
    void should_reclaim_stuck_events_when_found() {
        when(repository.reclaimStuck(
                eq(Constants.OutboxStatus.RETRY),
                any(LocalDateTime.class)
        )).thenReturn(3);

        watchdog.reclaim();

        verify(repository).reclaimStuck(
                eq(Constants.OutboxStatus.RETRY),
                any(LocalDateTime.class)
        );
    }

    @Test
    void should_do_nothing_when_no_stuck_events() {
        when(repository.reclaimStuck(any(), any()))
                .thenReturn(0);

        watchdog.reclaim();

        verify(repository).reclaimStuck(any(), any());
    }


    @Test
    void should_calculate_threshold_correctly() {
        ArgumentCaptor<LocalDateTime> captor =
                ArgumentCaptor.forClass(LocalDateTime.class);

        when(repository.reclaimStuck(any(), any())).thenReturn(0);

        LocalDateTime before = LocalDateTime.now();
        watchdog.reclaim();
        LocalDateTime after = LocalDateTime.now();

        verify(repository).reclaimStuck(eq(Constants.OutboxStatus.RETRY), captor.capture());

        LocalDateTime threshold = captor.getValue();
        assertThat(threshold)
                .isBefore(after)
                .isAfter(before.minusSeconds(40));
    }

}
