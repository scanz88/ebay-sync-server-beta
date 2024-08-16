package com.neutroware.ebaysyncserver.product;

import org.springframework.stereotype.Service;

@Service
public class ProductMapper {

    public ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .shopifyProductId(product.getShopifyProductId())
                .ebayItemId(product.getEbayItemId())
                .shopifyQuantity(product.getShopifyQuantity())
                .ebayQuantity(product.getEbayQuantity())
                .title(product.getTitle())
                .ebayPrice(product.getEbayPrice())
                .shopifyPrice(product.getShopifyPrice())
                .synced(product.getSynced())
                .createdDate(product.getCreatedDate())
                .build();
    }
}
