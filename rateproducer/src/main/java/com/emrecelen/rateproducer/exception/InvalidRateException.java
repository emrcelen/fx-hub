package com.emrecelen.rateproducer.exception;

public class InvalidRateException extends RuntimeException {

    private final String bid;
    private final String ask;

    public InvalidRateException(String message, String bid, String ask) {
        super(message);
        this.bid = bid;
        this.ask = ask;
    }

    public String getBid() {
        return bid;
    }

    public String getAsk() {
        return ask;
    }
}
