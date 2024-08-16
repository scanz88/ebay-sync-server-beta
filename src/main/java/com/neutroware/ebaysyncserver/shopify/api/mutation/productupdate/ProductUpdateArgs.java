package com.neutroware.ebaysyncserver.shopify.api.mutation.productupdate;

import java.util.List;

public record ProductUpdateArgs(
        ProductInput input
) {
    public record ProductInput(
            String id,
            List<String> tags
    ) {}
}
