package com.neutroware.ebaysyncserver.shopify.api.mutation.productcreate;

import java.util.List;

public record ProductCreateArgs(
        ProductInput input,
        List<Media> media
) {
    public record ProductInput(
            String descriptionHtml,
            List<String> tags,
            String title
    ) {}

    public record Media(
            String alt,
            String mediaContentType,
            String originalSource
    ) {}
}
