package com.neutroware.ebaysyncserver.shopify.api.mutation.inventoryadjustquantities;

import java.util.List;

public record InventoryAdjustQuantitiesArgs(
        List<InventoryAdjustQuantityInput> quantities
) {
    public record InventoryAdjustQuantityInput(
            Integer availableDelta,
            String inventoryItemId,
            String locationId,
            String reason
    ) {}
}
