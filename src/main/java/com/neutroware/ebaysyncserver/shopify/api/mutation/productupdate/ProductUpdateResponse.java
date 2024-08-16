package com.neutroware.ebaysyncserver.shopify.api.mutation.productupdate;

import com.neutroware.ebaysyncserver.shopify.api.util.type.Connection;

public record ProductUpdateResponse(
        Product product
) {
    public record Product(
            String id,
            String title,
            Integer totalInventory,
            Connection<Variant> variants
    ){}

    public record Variant(
            String id,
            InventoryItem inventoryItem
    ) {}

    public record InventoryItem(
            String id,
            Connection<InventoryLevel> inventoryLevels
    ) {}

    public record InventoryLevel(
            Location location
    ) {}

    public record Location(
            String id,
            String name
    ) {}
}

