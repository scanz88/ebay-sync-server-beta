package com.neutroware.ebaysyncserver.product;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ProductResponse(
        Long productId,
        String shopifyProductId,
        String ebayItemId,
        Integer ebayQuantity,
        Integer shopifyQuantity,
        String title,
        Float ebayPrice,
        Float shopifyPrice,
        Boolean synced,
        LocalDateTime createdDate
) {
}
