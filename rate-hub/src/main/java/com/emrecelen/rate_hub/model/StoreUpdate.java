package com.emrecelen.rate_hub.model;

import com.emrecelen.rate_hub.common.Constants.UpdateResult;

public record StoreUpdate(
        UpdateResult result,
        RateView view
) {
    public static StoreUpdate dropped(UpdateResult result) {
        return new StoreUpdate(result, null);
    }

    public static StoreUpdate kept(UpdateResult r, RateView v) {
        return new StoreUpdate(r, v);
    }

    public static StoreUpdate accepted(UpdateResult result, RateView view) {
        return new StoreUpdate(result, view);
    }
}
