package com.neutroware.ebaysyncserver.shopify.api.query.publications;

import com.neutroware.ebaysyncserver.shopify.api.util.service.GraphQlClientFactory;
import com.neutroware.ebaysyncserver.shopify.api.util.type.Edge;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Publications {
    private final GraphQlClientFactory graphQlClientFactory;

    public List<PublicationsResponse.Publication> getAllPublications(String storeName, String token) {
        HttpGraphQlClient client = graphQlClientFactory.create(storeName, token);
        List<PublicationsResponse.Publication> publications = new ArrayList<>();

        //first: 20 should get all the publications so no need for pagination
        //language=GraphQl
        String query = """
            query {
                publications(first: 20) { 
                    edges {
                        node {
                            id
                            name
                        }       
                    }       
                }       
            }       
        """;

        Mono<PublicationsResponse> monoResponse = client.document(query)
                .retrieve("publications")
                .toEntity(PublicationsResponse.class);

        PublicationsResponse response = monoResponse.block();

        return response.edges().stream()
                .map(Edge::node)
                .toList();
    }
}
