package com.neutroware.ebaysyncserver.shopify.api.query.products;

import com.neutroware.ebaysyncserver.shopify.api.util.type.Connection;
import com.neutroware.ebaysyncserver.shopify.api.util.type.Edge;

import java.util.List;

public record ProductsResponse(
    List<Edge<Product>> edges,
    PageInfo pageInfo
) {
    public record Product(
            String id,
            String title,
            Integer totalInventory,
            List<String> tags,
            Connection<Media> media
    ) {}

    public record PageInfo(
            Boolean hasPreviousPage,
            Boolean hasNextPage,
            String startCursor,
            String endCursor
    ) {}

    public record Media(
            String id
    ) {}
}

//public record ProductsResponse(
//        Connection<Product> products
//) {
//        public record Product(
//            String id,
//            String title,
//            Integer totalInventory,
//            List<String> tags
//    ) {}
//}
