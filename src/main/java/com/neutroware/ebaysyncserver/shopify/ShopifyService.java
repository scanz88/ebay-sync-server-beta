package com.neutroware.ebaysyncserver.shopify;

import com.neutroware.ebaysyncserver.activity.ActivityFlow;
import com.neutroware.ebaysyncserver.activity.ActivityService;
import com.neutroware.ebaysyncserver.activity.ActivityStatus;
import com.neutroware.ebaysyncserver.ebay.EbayService;
import com.neutroware.ebaysyncserver.ebay.EbayTransaction;
import com.neutroware.ebaysyncserver.ebay.api.enditem.EndItem;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItem;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItemResponse;
import com.neutroware.ebaysyncserver.ebay.api.reviseinventorystatus.ReviseInventoryStatus;
import com.neutroware.ebaysyncserver.encryption.EncryptionService;
import com.neutroware.ebaysyncserver.product.Product;
import com.neutroware.ebaysyncserver.product.ProductRepository;
import com.neutroware.ebaysyncserver.shopify.api.util.type.Order;
import com.neutroware.ebaysyncserver.userinfo.UserInfo;
import com.neutroware.ebaysyncserver.userinfo.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShopifyService {
    private final EncryptionService encryptionService;
    private final UserInfoRepository userInfoRepository;
    private final EbayService ebayService;
    private final ProductRepository productRepository;
    private final GetItem getItem;
    private final ReviseInventoryStatus reviseInventoryStatus;
    private final EndItem endItem;
    private final ActivityService activityService;

    public String getToken(String userId) {
        UserInfo userInfo = userInfoRepository.findById(userId).get();
        return encryptionService.decrypt(userInfo.getShopifyToken());
    }

    public String getStoreName(String userId) {
        UserInfo userInfo = userInfoRepository.findById(userId).get();
        return userInfo.getShopifyStoreName();
    }

    @Async("singleThreadExecutor")
    public void processOrder(String storeName, Order order) throws Exception {
        UserInfo userInfo = userInfoRepository.findByShopifyStoreName(storeName);
        String ebayToken = ebayService.refreshTokenIfExpired(userInfo.getUserId());
        List<Order.LineItem> lineItems = order.line_items();
        for (Order.LineItem lineItem : lineItems) {
            String shopifyProductId = "gid://shopify/Product/" + lineItem.product_id();
            Optional<Product> optionalProduct = productRepository.findByShopifyProductId(shopifyProductId);
            if (optionalProduct.isEmpty()) {
                continue;
            }
            Product product = optionalProduct.get();
            List<ShopifyOrder> previousOrders = product.getShopifyOrders();
            if (hasOrderId(previousOrders, order.admin_graphql_api_id())) {
                continue;
            }
            GetItemResponse ebayItem = getItem.getItem(ebayToken, product.getEbayItemId());
            Integer ebayCurrentQuantity = ebayItem.item().quantity() - ebayItem.item().sellingStatus().quantitySold();
            if (ebayCurrentQuantity - lineItem.quantity() > 0) {
                System.out.println("revise inventoty");
                reviseInventoryStatus.updateQuantity(ebayToken, ebayItem.item().itemId(), ebayCurrentQuantity - lineItem.quantity());
            } else {
                System.out.println("end listing");
                endItem.endListing(ebayToken, ebayItem.item().itemId());
            }
            product.setShopifyQuantity(product.getShopifyQuantity() - lineItem.quantity());
            product.setEbayQuantity(product.getEbayQuantity() - lineItem.quantity());
            ShopifyOrder shopifyOrder = new ShopifyOrder();
            shopifyOrder.setShopifyOrderId(order.admin_graphql_api_id());
            shopifyOrder.setCreatedAt(parseIsoOffsetDateTimeString(order.created_at()));
            shopifyOrder.setProduct(product);
            product.getShopifyOrders().add(shopifyOrder);
            productRepository.save(product);
            activityService.recordActivity(
                    ActivityFlow.SHOPIFY_TO_EBAY,
                    ActivityStatus.SUCCESS,
                    userInfo.getUserId(),
                    "Sold " + lineItem.quantity() + " of product " + product.getTitle()
            );
        }
        System.out.println("finished processing order");
    }

    public String extractStoreName(String shopDomain) {
        if (shopDomain == null || shopDomain.isEmpty()) {
            throw new IllegalArgumentException("Shop domain must not be null or empty");
        }

        String[] parts = shopDomain.split("\\.");
        System.out.println("extracted store name: " + parts[0]);
//        if (parts.length < 3) {
//            throw new IllegalArgumentException("Invalid Shopify shop domain format");
//        }

        return parts[0];
    }

    private boolean hasOrderId(List<ShopifyOrder> shopifyOrders, String orderIdToCheck) {
        for (ShopifyOrder order : shopifyOrders) {
            if (order.getShopifyOrderId().equals(orderIdToCheck)) {
                return true;
            }
        }
        return false;
    }

    private OffsetDateTime parseIsoOffsetDateTimeString(String isoOffsetDateTimeString) {
        return OffsetDateTime.parse(isoOffsetDateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
