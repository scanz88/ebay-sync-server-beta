package com.neutroware.ebaysyncserver.shopify.api.mutation.inventoryadjustquantities;

import java.util.List;

public record InventoryAdjustQuantitiesArgs(
        InventoryAdjustQuantitiesInput input
) {
    public record InventoryAdjustQuantitiesInput(
            String name,
            String reason,
            List<Change> changes
    ) {}

    public record Change(
            Integer delta,
            String inventoryItemId,
            String locationId
    ) {}
}
