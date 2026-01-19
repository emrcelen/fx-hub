package com.emrecelen.rateproducer.repository;

import com.emrecelen.rateproducer.model.OutboxEvent;
import com.emrecelen.rateproducer.common.Constants;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
               SELECT event FROM OutboxEvent  event
               WHERE event.status IN :statuses
               AND event.availableAt <= :now
               ORDER BY event.id
            """)
    List<OutboxEvent> lockNextBatch(
            @Param("statuses") List<Constants.OutboxStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
               SELECT event FROM OutboxEvent event
               WHERE event.status = :status
               ORDER BY event.id
            """)
    List<OutboxEvent> lockRetryBatch(
            @Param("status") Constants.OutboxStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Modifying
    @Query("""
            UPDATE OutboxEvent event
            SET event.status = :newStatus,
                event.processingStartedAt = null,
                event.lastError = 'PROCESSING timeout'
            WHERE event.status = 'PROCESSING'
            AND event.processingStartedAt < :threshold
            """)
    int reclaimStuck(
            @Param("newStatus") Constants.OutboxStatus newStatus,
            @Param("threshold") LocalDateTime threshold
    );

    @Query("SELECT count(e) FROM OutboxEvent e WHERE e.status = 'PENDING'")
    long countPending();
}
