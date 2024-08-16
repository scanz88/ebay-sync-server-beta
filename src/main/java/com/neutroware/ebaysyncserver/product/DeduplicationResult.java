package com.neutroware.ebaysyncserver.product;

import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItemResponse;
import com.neutroware.ebaysyncserver.shopify.api.query.products.ProductsResponse;

import java.util.List;
import java.util.Map;

//Partitions the items into identical, editDistOver30, and editDistUnder30
public record DeduplicationResult(
        //Map of identical ebay and shopify products (based on exact title match)
        Map<GetItemResponse.Item, ProductsResponse.Product> identical,
        List<GetItemResponse.Item> editDistOver30,
        List<GetItemResponse.Item> editDistUnder30
) {
}
