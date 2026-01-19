package com.emrecelen.rateproducer.exception;

public class PairNotActiveException extends RuntimeException {

    private final String pair;

    public PairNotActiveException(String pair) {
        super("Pair is not active");
        this.pair = pair;
    }

    public String getPair() {
        return pair;
    }
}
