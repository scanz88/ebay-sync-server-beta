package com.neutroware.ebaysyncserver.shopify.api.mutation.webhooksubscriptioncreate;

import com.neutroware.ebaysyncserver.shopify.api.util.service.GraphQlClientFactory;
import com.neutroware.ebaysyncserver.shopify.api.util.service.ThrottleService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WebhookSubscriptionCreate {

    private final GraphQlClientFactory graphQlClientFactory;
    private final ThrottleService throttleService;

    public WebhookSubscriptionCreateResponse registerWebhook(String storeName, String token, WebhookSubscriptionCreateArgs args) {
        HttpGraphQlClient client = graphQlClientFactory.create(storeName, token);

        //language=GraphQl
        String mutation = """
            mutation ($topic: WebhookSubscriptionTopic!, $webhookSubscription: WebhookSubscriptionInput!) {
                webhookSubscriptionCreate(topic: $topic, webhookSubscription: $webhookSubscription) {
                     webhookSubscription {
                       id
                       topic
                     }      
                     userErrors {
                        field
                        message
                    }
                }
            }
        """;

        Mono<WebhookSubscriptionCreateResponse> monoResponse = client.document(mutation)
                .variable("topic", args.topic())
                .variable("webhookSubscription", args.webhookSubscription())
                .execute()
                .map(gqlResponse -> {
                    if (!gqlResponse.isValid()) {
                        throw new RuntimeException("webhooksubscriptioncreate error: " + gqlResponse.toString());
                    }
                    throttleService.throttle(gqlResponse.getExtensions());
                    return gqlResponse.field("webhookSubscriptionCreate")
                            .toEntity(WebhookSubscriptionCreateResponse.class);
                });

        return monoResponse.block();
    }
}
