package com.neutroware.ebaysyncserver.shopify.api.mutation.productvariantupdate;

import com.neutroware.ebaysyncserver.shopify.api.util.service.GraphQlClientFactory;
import com.neutroware.ebaysyncserver.shopify.api.util.service.ThrottleService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductVariantUpdate {
    private final GraphQlClientFactory graphQlClientFactory;
    private final ThrottleService throttleService;

    public ProductVariantUpdateResponse updateVariant(String storeName, String token, ProductVariantUpdateArgs args) {
        HttpGraphQlClient client = graphQlClientFactory.create(storeName, token);

        //language=GraphQl
        String mutation = """
            mutation ($variants: [ProductVariantsBulkInput!]!) {
                productVariantsBulkUpdate(variants: $variants) {
                    productVariants {
                        id
                    }
                    userErrors {
                        field
                        message
                    }
                }
            }
        """;

        Mono<ProductVariantUpdateResponse> monoResponse = client.document(mutation)
                .variable("variants", List.of(args.input()))
                .execute()
                .map(gqlResponse -> {
                    if (!gqlResponse.isValid()) {
                        throw new RuntimeException("productvariantupdate error: " + gqlResponse.toString());
                    }
                    throttleService.throttle(gqlResponse.getExtensions());
                    return gqlResponse.field("productVariantsBulkUpdate")
                            .toEntity(ProductVariantUpdateResponse.class);
                });

        return monoResponse.block();
    }
}
