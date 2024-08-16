package com.neutroware.ebaysyncserver.shopify.api.util.type;

public record Edge<T>(
        T node,
        String cursor
) {}