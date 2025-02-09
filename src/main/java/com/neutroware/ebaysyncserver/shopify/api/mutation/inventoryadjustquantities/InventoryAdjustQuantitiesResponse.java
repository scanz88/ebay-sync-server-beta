package com.neutroware.ebaysyncserver.shopify.api.mutation.inventoryadjustquantities;

import java.util.List;

public record InventoryAdjustQuantitiesResponse(
    List<InventoryLevel> inventoryLevels,
    List<UserError> userErrors
) {
    public record InventoryLevel(
        String id,
        Integer available
    ) {}

    public record UserError(
        String field,
        String message
    ) {}
}
