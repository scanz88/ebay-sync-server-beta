package com.neutroware.ebaysyncserver.shopify.api.util.type;

import java.util.List;
import java.util.Optional;

public record Connection<T>(
        List<Edge<T>> edges,
        Optional<PageInfo> pageInfo
) {

    public record PageInfo(
            Boolean hasPreviousPage,
            Boolean hasNextPage,
            String startCursor,
            String endCursor
    ) {}
}
