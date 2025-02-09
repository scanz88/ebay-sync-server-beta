// ... existing code ...
@Configuration
public class ShopifyConfig {
    @Value("${shopify.api.version}")
    private String apiVersion = "2024-07";  // Update API version
    // ... existing code ...
}