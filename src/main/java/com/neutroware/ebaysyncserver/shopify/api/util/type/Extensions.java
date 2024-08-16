package com.neutroware.ebaysyncserver.shopify.api.util.type;

public record Extensions(
        Cost cost
) {
    public record Cost(
            Integer requestedQueryCost,
            Integer actualQueryCost,
            ThrottleStatus throttleStatus
    ) {}

    public record ThrottleStatus(
            Integer maximumAvailable,
            Integer currentlyAvailable,
            Integer restoreRate
    ) {}
}
