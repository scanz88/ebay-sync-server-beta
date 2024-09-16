package com.neutroware.ebaysyncserver.shopify.api.util.type;

import java.util.List;

public record ShippingRatesResponse(
        List<Rate> rates
) {
    public record Rate(
            String service_name,
            String service_code,
            String total_price,
            String description,
            String currency,
            Boolean phone_required,
            String min_delivery_date,
            String max_delivery_date
    ) {}
}