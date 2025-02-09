package com.neutroware.ebaysyncserver.shopify.api.mutation.mediaupdate;

import java.util.List;

public record ProductMediaUpdateArgs(
    String productId,
    List<MediaInput> media
) {
    public record MediaInput(
        String alt,
        String mediaContentType,
        String originalSource
    ) {}
}