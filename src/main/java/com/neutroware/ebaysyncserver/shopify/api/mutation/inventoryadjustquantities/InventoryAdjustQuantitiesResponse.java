package com.neutroware.ebaysyncserver.shopify.api.mutation.inventoryadjustquantities;

import com.neutroware.ebaysyncserver.shopify.api.util.type.UserError;

import java.util.List;

public record InventoryAdjustQuantitiesResponse(
        List<UserError> userErrors
) {

}
