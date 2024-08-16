package com.neutroware.ebaysyncserver.shopify.api.util.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Order(
        String admin_graphql_api_id,
        String created_at,
        List<LineItem> line_items
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LineItem(
            String product_id,
            Integer quantity,
            String title
    ) {}
}
