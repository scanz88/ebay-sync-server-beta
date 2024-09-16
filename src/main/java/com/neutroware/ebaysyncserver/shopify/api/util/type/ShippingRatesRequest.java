package com.neutroware.ebaysyncserver.shopify.api.util.type;

import java.util.List;

public record ShippingRatesRequest(
        Rate rate
) {
    public record Rate(
            Address origin,
            Address destination,
            List<Item> items,
            String currency,
            String locale
    ) {}

    public record Address(
            String country,
            String postal_code,
            String province,
            String city,
            String name,
            String address1,
            String address2,
            String phone,
            String fax,
            String email,
            String address_type,
            String company_name
    ) {}

    public record Item(
            String name,
            String sku,
            Integer quantity,
            Double grams,
            Double price,
            String vendor,
            Boolean requires_shipping,
            Boolean taxable,
            String fulfillment_service,
            Long product_id,
            Long variant_id
    ) {}

}
