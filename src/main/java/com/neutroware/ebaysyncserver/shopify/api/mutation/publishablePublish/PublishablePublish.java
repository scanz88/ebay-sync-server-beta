package com.neutroware.ebaysyncserver.shopify.api.mutation.publishablePublish;

import com.neutroware.ebaysyncserver.shopify.api.util.service.GraphQlClientFactory;
import com.neutroware.ebaysyncserver.shopify.api.util.service.ThrottleService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PublishablePublish {
    private final GraphQlClientFactory graphQlClientFactory;
    private final ThrottleService throttleService;

    public PublishablePublishResponse publishResource(String storeName, String token, PublishablePublishArgs args) {
        HttpGraphQlClient client = graphQlClientFactory.create(storeName, token);

        //language=GraphQl
        String mutation = """
            mutation ($id: ID!, $input: [PublicationInput!]!) {
                publishablePublish(id: $id, input: $input) {
                     userErrors {
                        field
                        message
                    }
                }
            }
        """;

        Mono<PublishablePublishResponse> monoResponse = client.document(mutation)
                .variable("id", args.id())
                .variable("input", args.input())
                .execute()
                .map(gqlResponse -> {
                    if (!gqlResponse.isValid()) {
                        throw new RuntimeException("publishablepublish error: " + gqlResponse.toString());
                    }
                    throttleService.throttle(gqlResponse.getExtensions());
                    return gqlResponse.field("publishablePublish")
                            .toEntity(PublishablePublishResponse.class);
                });

        return monoResponse.block();
    }
}
