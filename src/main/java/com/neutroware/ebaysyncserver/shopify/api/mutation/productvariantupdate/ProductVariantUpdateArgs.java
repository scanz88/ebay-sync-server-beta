package com.neutroware.ebaysyncserver.shopify.api.mutation.productvariantupdate;

public record ProductVariantUpdateArgs(
        ProductVariantsBulkInput input
) {
   public record ProductVariantsBulkInput(
           String id,
           String price,
           String sku,
           String inventoryQuantity,
           String inventoryPolicy,
           InventoryItem inventoryItem
   ) {}

    public record InventoryItem(
            Measurement measurement,
            Boolean tracked,
            Boolean requiresShipping
    ) {}

    public record Measurement(
            Weight weight
    ) {}

    public record Weight(
            String unit,
            Float value
    ) {}
}