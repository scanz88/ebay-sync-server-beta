package com.neutroware.ebaysyncserver.shopify.api.util.service;

import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GraphQlClientFactory {

    public HttpGraphQlClient create(String storeName, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Shopify-Access-Token", token);
        String baseUrl = String.format("https://%s.myshopify.com/admin/api/2024-04/graphql.json", storeName);
        WebClient client = WebClient.builder().baseUrl(baseUrl)
                .defaultHeaders(h -> h.addAll(headers)).build();
       return HttpGraphQlClient.builder(client).build();
    }
}
