package com.neutroware.ebaysyncserver.shopify.api.mutation.inventoryadjustquantities;

import com.neutroware.ebaysyncserver.shopify.api.util.service.GraphQlClientFactory;
import com.neutroware.ebaysyncserver.shopify.api.util.service.ThrottleService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class InventoryAdjustQuantities {
    private final GraphQlClientFactory graphQlClientFactory;
    private final ThrottleService throttleService;

    public InventoryAdjustQuantitiesResponse adjustQuantities(String storeName, String token, InventoryAdjustQuantitiesArgs args) {
        HttpGraphQlClient client = graphQlClientFactory.create(storeName, token);

        //language=GraphQl
        String mutation = """
            mutation ($quantities: [InventoryAdjustQuantityInput!]!) {
                inventoryBulkAdjustQuantityAtLocation(inventoryItemAdjustments: $quantities) {
                    inventoryLevels {
                        id
                        available
                    }
                    userErrors {
                        field
                        message
                    }
               }
           }
        """;

        Mono<InventoryAdjustQuantitiesResponse> monoResponse = client.document(mutation)
                .variable("quantities", args.input().changes())  // Need to use changes from input
                .execute()
                .map(gqlResponse -> {
                    if (!gqlResponse.isValid()) {
                        throw new RuntimeException("inventoryadjustquantities error: " + gqlResponse.toString());
                    }
                    throttleService.throttle(gqlResponse.getExtensions());
                    return gqlResponse.field("inventoryBulkAdjustQuantityAtLocation").toEntity(InventoryAdjustQuantitiesResponse.class);
                });

        return monoResponse.block();
    }
}
