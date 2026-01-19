package com.emrecelen.rateproducer.model;


import com.emrecelen.rateproducer.model.common.BaseEntity;
import com.emrecelen.rateproducer.common.Constants.OutboxType;
import com.emrecelen.rateproducer.common.Constants.OutboxStatus;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_event")
public class OutboxEvent extends BaseEntity {
    @Column(name = "event_key", nullable = false)
    private String eventKey;
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private OutboxType eventType;
    @Column(name = "schema_version", nullable = false)
    private int schemaVersion;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    private JsonNode payload;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxStatus status;
    @Column(name = "attempts", nullable = false)
    private int attempts;
    @Column(name = "available_at")
    private LocalDateTime availableAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;
    @Column(name = "last_error")
    private String lastError;

    protected OutboxEvent() {
    }

    public static OutboxEvent pending(String eventKey, OutboxType type, int schemaVersion, JsonNode payload) {
        OutboxEvent e = new OutboxEvent();
        e.eventKey = eventKey;
        e.eventType = type;
        e.schemaVersion = schemaVersion;
        e.payload = payload;
        e.status = OutboxStatus.PENDING;
        e.attempts = 0;
        e.availableAt = LocalDateTime.now();
        e.createdAt = LocalDateTime.now();
        return e;
    }

    public String getEventKey() {
        return eventKey;
    }

    public OutboxType getEventType() {
        return eventType;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public void setStatus(OutboxStatus status) {
        this.status = status;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public LocalDateTime getAvailableAt() {
        return availableAt;
    }

    public void setAvailableAt(LocalDateTime availableAt) {
        this.availableAt = availableAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public LocalDateTime getProcessingStartedAt() {
        return processingStartedAt;
    }

    public void setProcessingStartedAt(LocalDateTime processingStartedAt) {
        this.processingStartedAt = processingStartedAt;
    }
}
