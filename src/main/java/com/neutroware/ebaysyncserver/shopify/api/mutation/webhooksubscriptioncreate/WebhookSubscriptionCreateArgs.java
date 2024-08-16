package com.neutroware.ebaysyncserver.shopify.api.mutation.webhooksubscriptioncreate;

public record WebhookSubscriptionCreateArgs(
        String topic,
        WebhookSubscriptionInput webhookSubscription
) {
    public record WebhookSubscriptionInput(
            String callbackUrl,
            String format
            //List<String> includeFields
    ) {}
}
