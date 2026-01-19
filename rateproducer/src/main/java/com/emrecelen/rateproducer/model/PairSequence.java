package com.emrecelen.rateproducer.model;

import com.emrecelen.rateproducer.common.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "pair_sequence")
public class PairSequence {

    @Id
    @Column(name = "pair", nullable = false)
    private String pair;
    @Column(name = "last_seq", nullable = false)
    private Long lastSeq;

    protected PairSequence() {}

    private PairSequence(Builder builder) {
        this.pair = builder.pair;
        this.lastSeq = builder.lastSeq;
    }

    public String getPair() {
        return pair;
    }

    public Long getLastSeq() {
        return lastSeq;
    }

    public void increment() {
        this.lastSeq++;
    }

    public static Builder builder(String pair) {
        return new Builder(pair);
    }

    public static final class Builder {

        private final String pair;
        private Long lastSeq;

        private Builder(String pair) {
            if (pair == null || pair.isBlank()) {
                throw new IllegalArgumentException("pair must not be null or blank");
            }
            this.pair = pair;
        }

        public Builder seq(long sequence) {
            this.lastSeq = sequence;
            return this;
        }


        public PairSequence build() {
            return new PairSequence(this);
        }
    }

}
