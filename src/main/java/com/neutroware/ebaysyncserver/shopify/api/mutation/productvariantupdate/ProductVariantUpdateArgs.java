package com.neutroware.ebaysyncserver.shopify.api.mutation.productvariantupdate;

public record ProductVariantUpdateArgs(
        ProductVariantInput input
) {
   public record ProductVariantInput(
           String id,
           String price,
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