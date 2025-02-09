package com.neutroware.ebaysyncserver.shopify.api.mutation.mediaupdate;

import com.neutroware.ebaysyncserver.shopify.api.util.service.GraphQlClientFactory;
import com.neutroware.ebaysyncserver.shopify.api.util.service.ThrottleService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductMediaUpdate {
    private final GraphQlClientFactory graphQlClientFactory;
    private final ThrottleService throttleService;

    public ProductMediaUpdateResponse updateMedia(String storeName, String token, ProductMediaUpdateArgs args) {
        HttpGraphQlClient client = graphQlClientFactory.create(storeName, token);

        String mutation = """
            mutation ($productId: ID!, $media: [CreateMediaInput!]!) {
                productCreateMedia(productId: $productId, media: $media) {
                    media {
                        id
                        mediaContentType
                        status
                    }
                    mediaUserErrors {
                        field
                        message
                    }
                }
            }
            """;

        Mono<ProductMediaUpdateResponse> monoResponse = client.document(mutation)
                .variable("productId", args.productId())
                .variable("media", args.media())
                .execute()
                .map((gqlResponse) -> {
                    if (!gqlResponse.isValid()) {
                        throw new RuntimeException("media update error: " + gqlResponse.toString());
                    }
                    throttleService.throttle(gqlResponse.getExtensions());
                    return gqlResponse.field("productCreateMedia")
                            .toEntity(new ParameterizedTypeReference<ProductMediaUpdateResponse>(){});
                });

        return monoResponse.block();
    }
}