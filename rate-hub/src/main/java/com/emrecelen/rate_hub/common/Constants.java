package com.emrecelen.rate_hub.common;

public final class Constants {
    private Constants() {}

    public enum UpdateResult {
        ACCEPTED_UPDATED,
        KEPT_LAST_GOOD_TTL_REFRESHED,
        DROPPED_OLD_SEQ,
        DROPPED_INVALID_TRANSPORT,
        DROPPED_INVALID_SCHEMA,
        DROPPED_INVALID_DOMAIN
    }
}
