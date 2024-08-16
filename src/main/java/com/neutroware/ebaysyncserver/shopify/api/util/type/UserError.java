package com.neutroware.ebaysyncserver.shopify.api.util.type;

import java.util.List;

public record UserError(
        List<String> field,
        String message
) {
}
