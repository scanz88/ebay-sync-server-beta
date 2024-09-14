package com.neutroware.ebaysyncserver.shopify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neutroware.ebaysyncserver.product.ProductRepository;
import com.neutroware.ebaysyncserver.shopify.api.mutation.webhooksubscriptioncreate.WebhookSubscriptionCreate;
import com.neutroware.ebaysyncserver.shopify.api.query.products.Products;
import com.neutroware.ebaysyncserver.shopify.api.rest.Orders;
import com.neutroware.ebaysyncserver.shopify.api.util.service.HmacVerifier;
import com.neutroware.ebaysyncserver.shopify.api.util.type.Order;
import com.neutroware.ebaysyncserver.userinfo.UserInfoRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(
        origins = "*"
)
@RequestMapping("shopify")
@RequiredArgsConstructor
public class ShopifyController {
    private final WebhookSubscriptionCreate webhookSubscriptionCreate;
    private final ShopifyService shopifyService;
    private final HmacVerifier hmacVerifier;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProductRepository productRepository;
    private final UserInfoRepository userInfoRepository;
    private final Products products;
    private final Orders orders;

    @GetMapping("/registerWebhooks")
    public void registerWebhooks() {

    }

    @PostMapping("/webhook")
    @Async("singleThreadExecutor") //Make async so shopify will get immediate 200 response
    public void webhook (
            @RequestHeader(name = "X-Shopify-Shop-Domain", required = true) String domain,
            @RequestHeader(name = "X-Shopify-Hmac-Sha256", required = true) String hmac,
            HttpServletRequest request
    ) throws Exception
    {
        System.out.println("Shopify webhook recieved");
        System.out.println("Domain: " + domain);
        String rawBody = "";
        try (BufferedReader reader = request.getReader()) {
            // Check if the reader is not null
            if (reader != null) {
                // Read all lines and join them
                rawBody = reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            System.err.println("Error reading request body of shopify webhook: " + e.getMessage());
            throw e;
        }

        System.out.println("Order body:");
        System.out.println(rawBody);
        String storeName = shopifyService.extractStoreName(domain);
        String shopifyWebhookSignature = userInfoRepository.findByShopifyStoreName(storeName).getShopifyWebhookSignature();
        boolean verified = hmacVerifier.verify(rawBody, hmac, shopifyWebhookSignature);
        if (!verified) {
            throw new SecurityException("Invalid HMAC signature");
        }
        Order order = objectMapper.readValue(rawBody, Order.class);
        shopifyService.processOrder(storeName, order);
    }


}
