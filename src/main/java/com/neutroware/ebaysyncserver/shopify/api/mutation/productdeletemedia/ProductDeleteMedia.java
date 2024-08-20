package com.neutroware.ebaysyncserver.shopify.api.mutation.productdeletemedia;


import com.neutroware.ebaysyncserver.shopify.api.util.service.GraphQlClientFactory;
import com.neutroware.ebaysyncserver.shopify.api.util.service.ThrottleService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductDeleteMedia {
    private final GraphQlClientFactory graphQlClientFactory;
    private final ThrottleService throttleService;

    public ProductDeleteMediaResponse deleteMedia(String storeName, String token, ProductDeleteMediaArgs args) {
        HttpGraphQlClient client = graphQlClientFactory.create(storeName, token);

        //language=GraphQl
        String mutation = """
            mutation ($mediaIds: [ID!]!, $productId: ID!) {
                productDeleteMedia(mediaIds: $mediaIds, productId: $productId) {
                    deletedMediaIds
                }   
            }
            """;
        Mono<ProductDeleteMediaResponse> monoResponse = client.document(mutation)
                .variable("mediaIds", args.mediaIds())
                .variable("productId", args.productId())
                .execute()
                .map((gqlResponse) -> {
                    if (!gqlResponse.isValid()) {
                        throw new RuntimeException("productmediadelete error: " + gqlResponse.toString());
                    }
                    throttleService.throttle(gqlResponse.getExtensions());
                    return gqlResponse.field("productDeleteMedia")
                            .toEntity(new ParameterizedTypeReference<ProductDeleteMediaResponse>(){});
                });

        ProductDeleteMediaResponse response = monoResponse.block();

        return response;
    }
}
