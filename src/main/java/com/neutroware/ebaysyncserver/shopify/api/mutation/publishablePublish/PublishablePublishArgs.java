package com.neutroware.ebaysyncserver.shopify.api.mutation.publishablePublish;

import java.util.List;

public record PublishablePublishArgs(
        String id,
        List<PublicationInput> input
) {
    public record PublicationInput(
            String publicationId
    ) {}
}
