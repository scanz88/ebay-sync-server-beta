package com.neutroware.ebaysyncserver.shopify.api.mutation.productdeletemedia;

import java.util.List;

public record ProductDeleteMediaResponse(
    ProductDeleteMedia productDeleteMedia
) {
    record ProductDeleteMedia(
            List<String> deletedMediaIds
    ){}
}
