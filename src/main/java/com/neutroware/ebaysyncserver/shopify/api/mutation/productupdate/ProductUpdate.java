package com.neutroware.ebaysyncserver.shopify.api.mutation.productupdate;


import com.neutroware.ebaysyncserver.shopify.api.util.service.GraphQlClientFactory;
import com.neutroware.ebaysyncserver.shopify.api.util.service.ThrottleService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductUpdate {
    private final GraphQlClientFactory graphQlClientFactory;
    private final ThrottleService throttleService;

    public ProductUpdateResponse updateProduct(String storeName, String token, ProductUpdateArgs args) {
        HttpGraphQlClient client = graphQlClientFactory.create(storeName, token);

        //language=GraphQl
        String mutation = """
            mutation ($product: ProductInput!) {
                productUpdate(product: $product) {
                     product {
                          id
                          title
                          totalInventory
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
        Mono<ProductUpdateResponse> monoResponse = client.document(mutation)
                .variable("product", args.input())  // Changed from "input" to "product"
                .execute()
                .map((gqlResponse) -> {
                    if (!gqlResponse.isValid()) {
                        throw new RuntimeException("productupdate error: " + gqlResponse.toString());
                    }
                    throttleService.throttle(gqlResponse.getExtensions());
                    return gqlResponse.field("productUpdate")
                            .toEntity(new ParameterizedTypeReference<ProductUpdateResponse>(){});
                });

        ProductUpdateResponse response = monoResponse.block();

        return response;
    }
}
