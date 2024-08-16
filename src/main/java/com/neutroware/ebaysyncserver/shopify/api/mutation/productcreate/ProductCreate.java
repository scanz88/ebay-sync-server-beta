package com.neutroware.ebaysyncserver.shopify.api.mutation.productcreate;

import com.neutroware.ebaysyncserver.shopify.api.util.service.GraphQlClientFactory;
import com.neutroware.ebaysyncserver.shopify.api.util.service.ThrottleService;
import com.neutroware.ebaysyncserver.shopify.api.util.type.Extensions;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductCreate {

    private final GraphQlClientFactory graphQlClientFactory;
    private final ThrottleService throttleService;

    public ProductCreateResponse createProduct(String storeName, String token, ProductCreateArgs args) {
        HttpGraphQlClient client = graphQlClientFactory.create(storeName, token);

        //language=GraphQl
        String mutation = """
            mutation ($input: ProductInput!, $media: [CreateMediaInput!]) {
                productCreate(input: $input, media: $media) {
                     product {
                          id
                          title
                          variants(first: 1) {
                            edges {
                              node {
                                id
                                inventoryItem {
                                  id
                                  inventoryLevels(first: 1) {
                                    edges {
                                      node {
                                        location {
                                          id
                                          name
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                        userErrors {
                          field
                          message
                        }
                }
            }
            """;
        Mono<ProductCreateResponse> monoResponse = client.document(mutation)
                .variable("input", args.input())
                .variable("media", args.media())
                .execute()
                .map((gqlResponse) -> {
                    if (!gqlResponse.isValid()) {
                        throw new RuntimeException("productcreate error: " + gqlResponse.toString());
                    }
                    throttleService.throttle(gqlResponse.getExtensions());
                    return gqlResponse.field("productCreate")
                            .toEntity(new ParameterizedTypeReference<ProductCreateResponse>(){});
                });

        ProductCreateResponse response = monoResponse.block();

        return response;
    }
}
