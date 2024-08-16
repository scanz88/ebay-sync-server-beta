package com.neutroware.ebaysyncserver.shopify.api.mutation.webhooksubscriptioncreate;

import com.neutroware.ebaysyncserver.shopify.api.util.type.UserError;

import java.util.List;

public record WebhookSubscriptionCreateResponse(
        WebhookSubscription webhookSubscription,
        List<UserError> userErrors
) {

    public record WebhookSubscription(
            String id,
            String topic
    ) {}
}
