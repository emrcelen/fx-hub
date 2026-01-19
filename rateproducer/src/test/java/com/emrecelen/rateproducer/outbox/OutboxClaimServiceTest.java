package com.emrecelen.rateproducer.outbox;

import com.emrecelen.rateproducer.common.Constants;
import com.emrecelen.rateproducer.model.OutboxEvent;
import com.emrecelen.rateproducer.outbox.service.OutboxClaimService;
import com.emrecelen.rateproducer.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxClaimServiceTest {

    @Mock
    private OutboxEventRepository repository;

    private OutboxClaimService service;

    @BeforeEach
    void setUp() {
        service = new OutboxClaimService(repository);
    }

    @Test
    void should_claim_and_mark_events_as_processing() {
        OutboxEvent e1 = OutboxEvent.pending(
                "anyEventKey1",
                Constants.OutboxType.RATE_EVENT,
                1,
                null
        );
        e1.setStatus(Constants.OutboxStatus.PENDING);

        OutboxEvent e2 = OutboxEvent.pending(
                "anyEventKey2",
                Constants.OutboxType.RATE_EVENT,
                1,
                null
        );
        e2.setStatus(Constants.OutboxStatus.FAILED);

        List<OutboxEvent> events = List.of(e1, e2);

        when(repository.lockNextBatch(
                eq(List.of(Constants.OutboxStatus.PENDING)),
                any(LocalDateTime.class),
                eq(PageRequest.of(0, 200))
        )).thenReturn(events);
        List<OutboxEvent> result = service.claimBatch();

        assertThat(result).hasSize(2);

        for (OutboxEvent event : result) {
            assertThat(event.getStatus())
                    .isEqualTo(Constants.OutboxStatus.PROCESSING);

            assertThat(event.getProcessingStartedAt())
                    .isNotNull();
        }
        verify(repository).saveAll(events);
    }

    @Test
    void should_return_empty_list_when_no_events_available() {
        // given
        when(repository.lockNextBatch(
                anyList(),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of());

        // when
        List<OutboxEvent> result = service.claimBatch();

        // then
        assertThat(result).isEmpty();
        verify(repository).saveAll(List.of());
    }

}
