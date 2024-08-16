package com.neutroware.ebaysyncserver.shopify.api.rest;

import com.neutroware.ebaysyncserver.shopify.api.util.type.Order;

import java.util.List;

public record OrdersResponse(
        List<Order> orders
) { }
