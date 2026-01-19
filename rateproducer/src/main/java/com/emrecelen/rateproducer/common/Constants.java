package com.emrecelen.rateproducer.common;

public final class Constants {
    private Constants() {}

    public enum OutboxType{
        RATE_EVENT,
    }

    public enum OutboxStatus {
        PENDING,
        PROCESSING,
        SENT,
        RETRY,
        FAILED,
    }

}
