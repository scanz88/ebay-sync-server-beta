package com.neutroware.ebaysyncserver.shopify.api.mutation.productupdate;

public record ProductUpdateArgs(
        ProductInput product  // Changed from 'input' to 'product'
) {
    public record ProductInput(
            String id,
            List<String> tags,
            String title,          // Added common product fields
            String description,
            String vendor,
            String productType,
            Boolean published,
            List<VariantInput> variants
    ) {}

    public record VariantInput(
            String id,
            String sku,
            String price,
            String compareAtPrice,
            Integer inventoryQuantity,
            String inventoryPolicy
    ) {}
}
