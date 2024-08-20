package com.neutroware.ebaysyncserver.shopify.api.mutation.productdeletemedia;

import java.util.List;

public record ProductDeleteMediaArgs(
        List<String> mediaIds,
        String productId
) {
}
