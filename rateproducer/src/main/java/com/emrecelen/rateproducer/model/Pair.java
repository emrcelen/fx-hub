package com.emrecelen.rateproducer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "allowed_pair")
public class Pair {
    @Id
    @Column(name = "pair", nullable = false)
    private String currencyPair;
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Pair() {
    }

    private Pair(Builder builder) {
        this.currencyPair = builder.currencyPair;
        this.isActive = builder.isActive;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }


    public String getCurrencyPair() {
        return currencyPair;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static Builder builder(String currencyPair) {
        return new Builder(currencyPair);
    }

    public static final class Builder {

        private final String currencyPair;
        private boolean isActive = true;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt;

        private Builder(String currencyPair) {
            if (currencyPair == null || currencyPair.isBlank()) {
                throw new IllegalArgumentException("currencyPair must not be null or blank");
            }
            this.currencyPair = currencyPair;
        }

        public Builder active(boolean active) {
            this.isActive = active;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Pair build() {
            return new Pair(this);
        }
    }
}
