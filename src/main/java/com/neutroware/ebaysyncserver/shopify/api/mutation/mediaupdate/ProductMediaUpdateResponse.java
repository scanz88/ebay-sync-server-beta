package com.neutroware.ebaysyncserver.shopify.api.mutation.mediaupdate;

import java.util.List;

public record ProductMediaUpdateResponse(
    List<Media> media,
    List<MediaUserError> mediaUserErrors
) {
    public record Media(
        String id,
        String mediaContentType,
        String status
    ) {}

    public record MediaUserError(
        String field,
        String message
    ) {}
}