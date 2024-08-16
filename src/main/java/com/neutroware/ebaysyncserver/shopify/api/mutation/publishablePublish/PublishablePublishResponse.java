package com.neutroware.ebaysyncserver.shopify.api.mutation.publishablePublish;

import com.neutroware.ebaysyncserver.shopify.api.util.type.UserError;

import java.util.List;

public record PublishablePublishResponse(
        List<UserError> userErrors
) {
}
