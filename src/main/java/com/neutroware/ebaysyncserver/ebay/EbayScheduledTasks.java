package com.neutroware.ebaysyncserver.ebay;

import com.fasterxml.jackson.databind.JsonNode;
import com.neutroware.ebaysyncserver.activity.ActivityFlow;
import com.neutroware.ebaysyncserver.activity.ActivityService;
import com.neutroware.ebaysyncserver.activity.ActivityStatus;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItem;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItemResponse;
import com.neutroware.ebaysyncserver.ebay.api.getsellerevents.GetSellerEvents;
import com.neutroware.ebaysyncserver.ebay.api.getsellertransactions.GetSellerTransactions;
import com.neutroware.ebaysyncserver.product.*;
import com.neutroware.ebaysyncserver.shopify.ShopifyService;
import com.neutroware.ebaysyncserver.shopify.api.mutation.inventoryadjustquantities.InventoryAdjustQuantities;
import com.neutroware.ebaysyncserver.shopify.api.mutation.inventoryadjustquantities.InventoryAdjustQuantitiesArgs;
import com.neutroware.ebaysyncserver.shopify.api.query.products.Products;
import com.neutroware.ebaysyncserver.shopify.api.query.products.ProductsResponse;
import com.neutroware.ebaysyncserver.userinfo.UserInfo;
import com.neutroware.ebaysyncserver.userinfo.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//TODO: add api to monitor how many calls I have left out of 5000 and adjust polling intervals as needed

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EbayScheduledTasks {
    private final UserInfoRepository userInfoRepository;
    private final GetSellerEvents getSellerEvents;
    private final EbayService ebayService;
    private final ProductRepository productRepository;
    private final GetItem getItem;
    private final Products products;
    private final ImportUtils importUtils;
    private final Importer importer;
    private final ShopifyService shopifyService;
    private final GetSellerTransactions getSellerTransactions;
    private final InventoryAdjustQuantities inventoryAdjustQuantities;
    private final ActivityService activityService;

    @Scheduled(fixedRate = 60000)
    @Async("singleThreadExecutor")
    public void pollForNewListings() throws Exception {
        System.out.println("running new lisitngs");
        List<UserInfo> userInfoList = userInfoRepository.findAll();

        for (UserInfo userInfo : userInfoList) {
            if (userInfo.getInitiated()) {
                Instant now = Instant.now();
                Instant fiveMinutesAgo = now.minusSeconds(300);
                DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);
                String startTimeFrom = formatter.format(fiveMinutesAgo);
                String startTimeTo = formatter.format(now);
                String ebayToken = ebayService.refreshTokenIfExpired(userInfo.getUserId());
                List<GetItemResponse.Item> newEbayItems = new ArrayList<>();

                JsonNode response = getSellerEvents.getSellerEvents(ebayToken, startTimeFrom, startTimeTo);
                JsonNode itemArray = response.at("/ItemArray");
                if (!itemArray.isEmpty()) {
                   JsonNode item = itemArray.at("/Item");
                   if (item.isArray()) {
                       for (JsonNode itemNode : item) {
                           collectNewItems(userInfo, itemNode, newEbayItems);
                       }
                   } else {
                     collectNewItems(userInfo, item, newEbayItems);
                   }
                   if (!newEbayItems.isEmpty()) {
                       importNewListings(userInfo, newEbayItems);
                   }

                }
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    @Async("singleThreadExecutor")
    public void pollForNewSales() throws Exception {
        System.out.println("polling for new salses");
        List<UserInfo> userInfoList = userInfoRepository.findAll();

        for (UserInfo userInfo : userInfoList) {
            if (userInfo.getInitiated()) {
                Instant now = Instant.now();
                Instant fiveMinutesAgo = now.minusSeconds(300);
                DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);
                String modTimeFrom = formatter.format(fiveMinutesAgo);
                String modTimeTo = formatter.format(now);
                String ebayToken = ebayService.refreshTokenIfExpired(userInfo.getUserId());

                JsonNode response = getSellerTransactions.getSellerTransactions(ebayToken, modTimeFrom, modTimeTo);
                System.out.println("sales response");
                System.out.println(response.toPrettyString());
                int returnedTransactionCountActual = response.at("/ReturnedTransactionCountActual").asInt();

                if (returnedTransactionCountActual > 0) {
                    JsonNode transactionArray = response.at("/TransactionArray");
                    if (!transactionArray.isEmpty()) {
                        JsonNode transaction = transactionArray.at("/Transaction");
                        if (transaction.isArray()) {
                            for (JsonNode transactionNode : transaction) {
                                processTransaction(userInfo, transactionNode);
                            }
                        } else {
                           processTransaction(userInfo, transaction);
                        }
                    }
                }
            }
        }
    }

    private void processTransaction(UserInfo userInfo, JsonNode transaction) {
        String ebayItemId = transaction.at("/Item/ItemID").asText();
        String transactionId = transaction.at("/TransactionID").asText();
        Integer quantityPurchased = transaction.at("/QuantityPurchased").asInt();
        String paidTime = transaction.at("/PaidTime").asText();

        Optional<Product> optionalProduct = productRepository.findByEbayItemId(ebayItemId);
        if(optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            List<EbayTransaction> previousEbayTransactions = product.getEbayTransactions();
            if (!hasTransactionId(previousEbayTransactions, transactionId)) {
                String shopifyInventoryItemId = product.getShopifyInventoryItemId();
                String shopifyLocationId = product.getShopifyInventoryLocationId();
                String shopifyToken = shopifyService.getToken(userInfo.getUserId());
                InventoryAdjustQuantitiesArgs inventoryArgs = new InventoryAdjustQuantitiesArgs(
                        new InventoryAdjustQuantitiesArgs.InventoryAdjustQuantitiesInput(
                                "available",
                                "correction",
                                new ArrayList<>(List.of(
                                        new InventoryAdjustQuantitiesArgs.Change(
                                                -quantityPurchased,
                                                shopifyInventoryItemId,
                                                shopifyLocationId
                                        )
                                ))
                        )
                );
                inventoryAdjustQuantities.adjustQuantities(userInfo.getShopifyStoreName(), shopifyToken, inventoryArgs);
                product.setEbayQuantity(product.getEbayQuantity() - quantityPurchased);
                product.setShopifyQuantity(product.getShopifyQuantity() - quantityPurchased);
                EbayTransaction ebayTransaction = new EbayTransaction();
                ebayTransaction.setEbayTransactionId(transactionId);
                ebayTransaction.setProduct(product);
                ebayTransaction.setPaidTime(LocalDateTime.ofInstant(Instant.parse(paidTime), ZoneId.of("UTC")));
                product.getEbayTransactions().add(ebayTransaction);
                productRepository.save(product);
                activityService.recordActivity(
                        ActivityFlow.EBAY_TO_SHOPIFY,
                        ActivityStatus.SUCCESS,
                        userInfo.getUserId(),
                        "Sold " + quantityPurchased + " of product " + product.getTitle()
                );
            }
        }
    }

    // Method to check if a transaction ID exists in the list
    private boolean hasTransactionId(List<EbayTransaction> ebayTransactions, String transactionIdToCheck) {
        for (EbayTransaction transaction : ebayTransactions) {
            if (transaction.getEbayTransactionId().equals(transactionIdToCheck)) {
                return true; // Found the transaction ID
            }
        }
        return false; // Transaction ID not found
    }

    private void collectNewItems(UserInfo userInfo, JsonNode item, List<GetItemResponse.Item> newEbayItems) throws Exception {
        String itemId = item.at("/ItemID").asText();
        String title = item.at("/Title").asText().trim();
        Optional<Product> optionalProduct = productRepository.findByEbayItemId(itemId);
        if (optionalProduct.isEmpty()) {
            Optional<Product> productByTitle = productRepository.findByTitle(title);
            //If title exists in DB, delete product from DB since the importer will just create a new, up-to-date product entity
            productByTitle.ifPresent(productRepository::delete);
            GetItemResponse.Item newItem = getItem.getItem(ebayService.refreshTokenIfExpired(userInfo.getUserId()), itemId).item();
            newEbayItems.add(newItem);
        }
    }

    private void importNewListings(UserInfo userInfo, List<GetItemResponse.Item> newEbayItems) {
        System.out.println("importing!");
        String shopifyToken = shopifyService.getToken(userInfo.getUserId());
        List<ProductsResponse.Product> allShopifyProds = products.getAllProducts(userInfo.getShopifyStoreName(), shopifyToken);
        DeduplicationResult dedupResult = importUtils.deduplicate(newEbayItems, allShopifyProds);
        importer.importFromEbayToShopify(dedupResult, userInfo.getEbayToken(), shopifyToken, userInfo.getShopifyStoreName(), userInfo.getUserId());
    }
}
