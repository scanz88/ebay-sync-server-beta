package com.neutroware.ebaysyncserver.shopify.api.query.publications;

import com.neutroware.ebaysyncserver.shopify.api.util.type.Edge;

import java.util.List;

public record PublicationsResponse(
        List<Edge<Publication>> edges
) {
    public record Publication(
            String id,
            String name
    ) {}
}
