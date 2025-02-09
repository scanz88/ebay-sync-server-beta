package com.neutroware.ebaysyncserver.shopify.api.mutation.productvariantupdate;

import java.util.List;

public record ProductVariantUpdateResponse(
    List<UserError> userErrors
) {
    public record UserError(
        String field,
        String message
    ) {}
}
