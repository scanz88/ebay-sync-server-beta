package com.neutroware.ebaysyncserver.shopify.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neutroware.ebaysyncserver.shopify.api.util.type.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Orders {
    private final RestTemplate restTemplate;

    public List<Order> getOrders(String storeName, String token, String createdAtMin) {
        String baseUrl = String.format("https://%s.myshopify.com/admin/api/2024-04/orders.json", storeName);
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("status", "any")
                .queryParam("created_at_min", createdAtMin)
                .toUriString();
        HttpHeaders headers= new HttpHeaders();
        headers.set("X-Shopify-Access-Token", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<OrdersResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, OrdersResponse.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to retrieve shopify orders: " + response.getBody());
        }
        return response.getBody().orders();
    }
}
