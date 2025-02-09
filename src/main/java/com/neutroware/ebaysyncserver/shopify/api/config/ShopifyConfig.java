package com.neutroware.ebaysyncserver.shopify.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShopifyConfig {
    @Value("${shopify.api.version}")
    private String apiVersion = "2024-07";  // Updated to latest required version
    
    @Value("${shopify.scopes}")
    private String scopes = "read_products,write_products,read_locations";  // Added read_locations scope
    
    public String getApiVersion() {
        return apiVersion;
    }
    
    public String getScopes() {
        return scopes;
    }
}