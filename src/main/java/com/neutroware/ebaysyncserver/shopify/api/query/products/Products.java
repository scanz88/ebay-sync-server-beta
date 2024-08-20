package com.neutroware.ebaysyncserver.shopify.api.query.products;

import com.neutroware.ebaysyncserver.shopify.api.util.service.GraphQlClientFactory;
import com.neutroware.ebaysyncserver.shopify.api.util.service.ThrottleService;
import com.neutroware.ebaysyncserver.shopify.api.util.type.Edge;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Products {

    private final GraphQlClientFactory graphQlClientFactory;
    private final ThrottleService throttleService;

    public List<ProductsResponse.Product> getAllProducts(String storeName, String token) {
        HttpGraphQlClient client = graphQlClientFactory.create(storeName, token);
        List<ProductsResponse> products = new ArrayList<>();

        //language=GraphQl
        String queryTemplate = """
                query($first: Int, $after: String) {
                    products(first: $first, after: $after) {
                        edges {
                            node {
                                id
                                title
                                totalInventory
                                tags
                                 media(first: 50) {
                                      edges {
                                        node {
                                          id
                                      }
                                    }
                                  }
                            }
                            cursor
                        }
                        pageInfo {
                            hasPreviousPage
                            hasNextPage
                            startCursor
                            endCursor
                        }
                    }
                }
                """;
        String cursor = null;
        boolean hasNextPage;

        do {
            String query = queryTemplate;

            Mono<ProductsResponse> connectionMono = client.document(query)
                    .variable("first", 250)
                    .variable("after", cursor)
                    .execute()
                    .map(gqlResponse -> {
                        throttleService.throttle(gqlResponse.getExtensions());
                        return gqlResponse.field("products")
                                .toEntity(ProductsResponse.class);
                    });


            // The spring graphql client will throw exception when errors occur (see org.springframework.graphql.client.GraphQlClientException)
            // So we can use usual try-catch to handle graphql errors
            //Need to also be able to capture shopify's unique userErrors field, which I'm not sure if the spring client will throw
            // exception for since in a sample it is included under the data/{entity} object (userErrors needs to be requested and
            // is only provided for mutations)

            ProductsResponse connection = connectionMono.block();
            Objects.requireNonNull(connection);

            products.add(connection);
            cursor = connection.pageInfo().endCursor();
            hasNextPage = connection.pageInfo().hasNextPage();

        } while (hasNextPage);

        return parseProductsResponse(products);
    }

    private List<ProductsResponse.Product> parseProductsResponse(List<ProductsResponse> response) {
        return response.stream()
                .flatMap(productsResponse -> productsResponse.edges().stream())
                .map(Edge::node)
                .collect(Collectors.toList());
    }
}
