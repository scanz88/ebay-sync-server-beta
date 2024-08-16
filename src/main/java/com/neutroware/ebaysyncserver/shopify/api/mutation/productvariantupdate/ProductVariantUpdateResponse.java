package com.neutroware.ebaysyncserver.shopify.api.mutation.productvariantupdate;

public record ProductVariantUpdateResponse(
        ProductVariant productVariant
) {
    public record ProductVariant(
            String id
    ) {}
}
