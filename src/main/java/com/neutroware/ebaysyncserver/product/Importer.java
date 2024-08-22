package com.neutroware.ebaysyncserver.product;

import com.neutroware.ebaysyncserver.activity.ActivityFlow;
import com.neutroware.ebaysyncserver.activity.ActivityService;
import com.neutroware.ebaysyncserver.activity.ActivityStatus;
import com.neutroware.ebaysyncserver.ebay.EbayService;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItem;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItemResponse;
import com.neutroware.ebaysyncserver.ebay.api.getsellerlist.GetSellerList;
import com.neutroware.ebaysyncserver.ebay.api.getsellerlist.GetSellerListResponse;
import com.neutroware.ebaysyncserver.longjob.LongJob;
import com.neutroware.ebaysyncserver.longjob.LongJobRespository;
import com.neutroware.ebaysyncserver.longjob.LongJobStatus;
import com.neutroware.ebaysyncserver.longjob.LongJobType;
import com.neutroware.ebaysyncserver.shopify.ShopifyService;
import com.neutroware.ebaysyncserver.shopify.api.mutation.inventoryadjustquantities.InventoryAdjustQuantities;
import com.neutroware.ebaysyncserver.shopify.api.mutation.inventoryadjustquantities.InventoryAdjustQuantitiesArgs;
import com.neutroware.ebaysyncserver.shopify.api.mutation.productcreate.ProductCreate;
import com.neutroware.ebaysyncserver.shopify.api.mutation.productcreate.ProductCreateArgs;
import com.neutroware.ebaysyncserver.shopify.api.mutation.productdeletemedia.ProductDeleteMedia;
import com.neutroware.ebaysyncserver.shopify.api.mutation.productdeletemedia.ProductDeleteMediaArgs;
import com.neutroware.ebaysyncserver.shopify.api.mutation.productupdate.ProductUpdate;
import com.neutroware.ebaysyncserver.shopify.api.mutation.productupdate.ProductUpdateArgs;
import com.neutroware.ebaysyncserver.shopify.api.mutation.productvariantupdate.ProductVariantUpdate;
import com.neutroware.ebaysyncserver.shopify.api.mutation.productvariantupdate.ProductVariantUpdateArgs;
import com.neutroware.ebaysyncserver.shopify.api.mutation.publishablePublish.PublishablePublish;
import com.neutroware.ebaysyncserver.shopify.api.mutation.publishablePublish.PublishablePublishArgs;
import com.neutroware.ebaysyncserver.shopify.api.query.products.Products;
import com.neutroware.ebaysyncserver.shopify.api.query.products.ProductsResponse;
import com.neutroware.ebaysyncserver.shopify.api.query.publications.Publications;
import com.neutroware.ebaysyncserver.userinfo.UserInfo;
import com.neutroware.ebaysyncserver.userinfo.UserInfoRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class Importer {
    private final UserInfoRepository userInfoRepository;
    private final ProductRepository productRepository;
    private final ShopifyService shopifyService;
    private final EbayService ebayService;
    private final Products products;
    private final ProductCreate productCreate;
    private final ProductVariantUpdate productVariantUpdate;
    private final InventoryAdjustQuantities inventoryAdjustQuantities;
    private final PublishablePublish publishablePublish;
    private final Publications publications;
    private final GetSellerList getSellerList;
    private final ImportUtils importUtils;
    private final GetItem getItem;
    private final ProductUpdate productUpdate;
    private final LongJobRespository longJobRespository;
    private final EntityManager entityManager;
    private final ActivityService activityService;
    private final Map<String, String> failedItemsReport = new HashMap<>();
    private int successfullyImportedCount = 0;
    private final ProductDeleteMedia productDeleteMedia;

    public void retryFailedImport(String userId) throws Exception {
        UserInfo userInfo = userInfoRepository.findById(userId).orElseThrow();
        String ebayToken = ebayService.refreshTokenIfExpired(userInfo.getUserId());
        String shopifyToken = shopifyService.getToken(userId);
        String storeName = shopifyService.getStoreName(userId);

        failedItemsReport.clear();
        successfullyImportedCount = 0;

        List<Product> failedProducts = productRepository.findBySyncedFalseAndUserId(userInfo.getUserId());
        List<GetItemResponse.Item> allEbayProds = getSellerList.getAllActiveListings(ebayToken).stream()
                .map(GetSellerListResponse::itemArray)
                .filter(itemArray -> itemArray != null)
                .map(GetSellerListResponse.ItemArray::items)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        List<ProductsResponse.Product> allShopifyProds = products.getAllProducts(storeName, shopifyToken);
        DeduplicationResult dedupResult = importUtils.deduplicate(allEbayProds, allShopifyProds);

        Map<GetItemResponse.Item, ProductsResponse.Product> filteredIdentical = dedupResult.identical().entrySet().stream()
                .filter(entry -> failedProducts.stream().anyMatch(product -> product.getEbayItemId().equals(entry.getKey().itemId())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.println( "filteredIdentical" + filteredIdentical);

        List<GetItemResponse.Item> filteredEditDistOver30 = dedupResult.editDistOver30().stream()
                .filter(item -> failedProducts.stream().anyMatch(product -> product.getEbayItemId().equals(item.itemId())))
                .toList();
        System.out.println( "filteredover30" + filteredEditDistOver30);

        List<GetItemResponse.Item> filteredEditDistUnder30 = dedupResult.editDistUnder30().stream()
                .filter(item -> failedProducts.stream().anyMatch(product -> product.getEbayItemId().equals(item.itemId())))
                .toList();
        System.out.println( "filteredunder30" + filteredEditDistUnder30);

        DeduplicationResult filteredDedupResult = new DeduplicationResult(
                filteredIdentical,
                filteredEditDistOver30,
                filteredEditDistUnder30
        );

        productRepository.deleteAll(failedProducts);
        System.out.println("deleted products");

        executeImport(filteredDedupResult, ebayToken, shopifyToken, storeName, userInfo.getUserId(), null);

        System.out.println("Import complete!" + " " + successfullyImportedCount + " products were imported successfully." + " "
                + failedItemsReport.size() + " products " +
                "experienced failures during import"
        );
        System.out.println("Failed items report:");
        System.out.println(failedItemsReport.toString());
    }

    @Transactional
    @Async("singleThreadExecutor")
    public void findDuplicateShopifyProducts(String userId) {
        UserInfo userInfo = userInfoRepository.findById(userId).orElseThrow();
        String shopifyToken = shopifyService.getToken(userId);
        String storeName = shopifyService.getStoreName(userId);
        List<ProductsResponse.Product> allShopifyProds = products.getAllProducts(storeName, shopifyToken);

        Map<String, Long> titleCounts = allShopifyProds.stream()
                .collect(Collectors.groupingBy(
                        ProductsResponse.Product::title,
                        Collectors.counting()
                ));
        List<String> duplicateTitles = titleCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<ProductsResponse.Product> duplicateProducts = allShopifyProds.stream()
                .filter(product -> duplicateTitles.contains(product.title()))
                .collect(Collectors.toList());

        duplicateProducts.forEach(product ->
                System.out.println("Duplicate Product: " + product.title())
        );
    }

    @Transactional
    @Async("singleThreadExecutor")
    public void restoreFailedMedia(String userId) throws Exception {
        UserInfo userInfo = userInfoRepository.findById(userId).orElseThrow();
        String ebayToken = ebayService.refreshTokenIfExpired(userId);
        String shopifyToken = shopifyService.getToken(userId);
        String storeName = shopifyService.getStoreName(userId);

        List<GetItemResponse.Item> allEbayProds = getSellerList.getAllActiveListings(ebayToken).stream()
                .map(GetSellerListResponse::itemArray)
                .filter(itemArray -> itemArray != null)
                .map(GetSellerListResponse.ItemArray::items)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        List<ProductsResponse.Product> allShopifyProds = products.getAllProducts(storeName, shopifyToken);
        DeduplicationResult dedupResult = importUtils.deduplicate(allEbayProds, allShopifyProds);

        var result = new HashMap<String,Integer>();
        int count =0;

        for (Map.Entry<GetItemResponse.Item, ProductsResponse.Product> entry : dedupResult.identical().entrySet()) {
            GetItemResponse.Item ebayItem = entry.getKey();
            ProductsResponse.Product shopifyProduct = entry.getValue();

            int ebayImageCount = ebayItem.pictureDetails().pictureURL().size();
            int shopifyImageCount = shopifyProduct.media().edges().size();

            int diff = ebayImageCount - shopifyImageCount;

            if (diff != 0) {
                List<String> mediaIdsToDelete = shopifyProduct.media().edges().stream()
                        .map(edge -> edge.node().id())
                        .toList();
                ProductDeleteMediaArgs productDeleteMediaArgs =
                        new ProductDeleteMediaArgs(mediaIdsToDelete, shopifyProduct.id());
                var productDeleteMediaResult = productDeleteMedia.deleteMedia(
                        storeName,
                        shopifyToken,
                        productDeleteMediaArgs
                );
                Thread.sleep(1000);
                var media = importUtils.buildMediaList(ebayItem);
                ProductUpdateArgs productUpdateArgsArgs = new ProductUpdateArgs(
                        new ProductUpdateArgs.ProductInput(
                                shopifyProduct.id(),
                                shopifyProduct.tags()

                        ),
                        media
                );
                var productUpdateResult = productUpdate.updateProduct(storeName, shopifyToken, productUpdateArgsArgs);
                System.out.println("finished updating media of product: " + ebayItem.title());
                count++;
            }
        }
        System.out.println("restored media of " + count + " products");
    }

    @Transactional
    @Async("singleThreadExecutor")
    public void auxBulkImportFromEbayToShopify(String userId) throws Exception {
        UserInfo userInfo = userInfoRepository.findById(userId).orElseThrow();
        String ebayToken = ebayService.refreshTokenIfExpired(userId);
        String shopifyToken = shopifyService.getToken(userId);
        String storeName = shopifyService.getStoreName(userId);

        List<GetItemResponse.Item> allEbayProds = getSellerList.getAllActiveListings(ebayToken).stream()
                .map(GetSellerListResponse::itemArray)
                .filter(itemArray -> itemArray != null)
                .map(GetSellerListResponse.ItemArray::items)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        List<ProductsResponse.Product> allShopifyProds = products.getAllProducts(storeName, shopifyToken);
        DeduplicationResult dedupResult = importUtils.deduplicate(allEbayProds, allShopifyProds);

        System.out.println("potential dup: " + dedupResult.editDistUnder30().stream().map(GetItemResponse.Item::title).toList());
        System.out.println("new:  " + dedupResult.editDistOver30().stream().map(GetItemResponse.Item::title).toList());

        for (GetItemResponse.Item ebayItem : dedupResult.editDistOver30()) {
            try {
                if (Thread.currentThread().isInterrupted()) break;
                importUtils.addNewShopifyProduct(ebayItem, ebayToken, storeName, shopifyToken, userId);
            } catch (Exception e) {
                System.out.println("Import failed (adding new shopify product) on ebay item id" + ebayItem.itemId());
                System.out.println(e.getMessage());
            }
        }
    }

    //TODO: when making this call from UI (using @Async), the UI will poll DB
    // to check if finished. Make sure to also make it granular (like adding
    // how many items imported so far) to bulk operation entry in DB.
    // Also, when complete, have a flag that tells whether it partially failed
    // and that flag will be used to then trigger the retry operation from the UI
    // as specified in another comment
    @Transactional
    @Async("singleThreadExecutor")
    public void bulkImportFromEbayToShopify(String userId) throws Exception {
        UserInfo userInfo = userInfoRepository.findById(userId).orElseThrow();
        String ebayToken = ebayService.refreshTokenIfExpired(userId); //TODO: fix refresh problem and also create method to force refresh
        String shopifyToken = shopifyService.getToken(userId);
        String storeName = shopifyService.getStoreName(userId);

        LongJob longJob = importUtils.initializeLongJob(userId);
        failedItemsReport.clear();
        successfullyImportedCount = 0;

        List<GetItemResponse.Item> allEbayProds = getSellerList.getAllActiveListings(ebayToken).stream()
                .map(GetSellerListResponse::itemArray)
                .filter(itemArray -> itemArray != null)
                .map(GetSellerListResponse.ItemArray::items)
                .flatMap(List::stream)
               //.limit(10) //TODO: remove limit (only for testing)
                .collect(Collectors.toList());
        List<ProductsResponse.Product> allShopifyProds = products.getAllProducts(storeName, shopifyToken);
        DeduplicationResult dedupResult = importUtils.deduplicate(allEbayProds, allShopifyProds);

        executeImport(dedupResult, ebayToken, shopifyToken, storeName, userInfo.getUserId(), longJob.getLongJobId());

        activityService.recordActivity(
                ActivityFlow.EBAY_TO_SHOPIFY,
                ActivityStatus.SUCCESS,
                userId,
                "Imported " + successfullyImportedCount + " products"
        );

        System.out.println("Import complete!" + " " + successfullyImportedCount + " products were imported successfully." + " "
                + failedItemsReport.size() + " products " +
                "experienced failures during import"
        );
        System.out.println("Failed items report:");
        System.out.println(failedItemsReport.toString());
    }

    public void importFromEbayToShopify(
            DeduplicationResult dedupResult,
            String ebayToken,
            String shopifyToken,
            String storeName,
            String userId
    ) {
        failedItemsReport.clear();
        successfullyImportedCount = 0;
        executeImport(dedupResult, ebayToken, shopifyToken, storeName, userId, null);

        try {
            recordActivityOfImportFromEbayToShopify(userId, dedupResult);
        } catch(Exception e) {
            System.out.println("Failed to form record activity string: " + e.getMessage());
            activityService.recordActivity(
                    ActivityFlow.EBAY_TO_SHOPIFY,
                    ActivityStatus.SUCCESS,
                    userId,
                    "Imported " + successfullyImportedCount + " products"
            );
        }


        System.out.println("Import complete!" + " " + successfullyImportedCount + " products were imported successfully." + " "
                + failedItemsReport.size() + " products " +
                "experienced failures during import"
        );
        System.out.println("Failed items report:");
        System.out.println(failedItemsReport.toString());

    }

    private void recordActivityOfImportFromEbayToShopify(String userId, DeduplicationResult dedupResult) {
        String productString = successfullyImportedCount > 1 ? "products" : "product";
        StringBuilder description = new StringBuilder("Imported " + successfullyImportedCount + " " + productString + ": ");

        String titleList = Stream.concat(
                Stream.concat(
                        dedupResult.identical().keySet().stream(),
                        dedupResult.editDistOver30().stream()
                ),
                dedupResult.editDistUnder30().stream()
        )
                .map(GetItemResponse.Item::title)
                .collect(Collectors.joining(", "));

        description.append(titleList);

        activityService.recordActivity(
                ActivityFlow.EBAY_TO_SHOPIFY,
                ActivityStatus.SUCCESS,
                userId,
                description.toString()
        );
    }

    public void executeImport(
            DeduplicationResult dedupResult,
            String ebayToken,
            String shopifyToken,
            String storeName,
            String userId,
            Long longJobId
    ) {
        int totalCount = dedupResult.editDistOver30().size() + dedupResult.identical().size() + dedupResult.editDistUnder30().size();
        int currentCount = 0;
        for (GetItemResponse.Item ebayItem : dedupResult.editDistOver30()) {
            try {
                if (Thread.currentThread().isInterrupted()) break;
                importUtils.addNewShopifyProduct(ebayItem, ebayToken, storeName, shopifyToken, userId);
                successfullyImportedCount++;
                currentCount++;
                System.out.println(successfullyImportedCount + " products imported...");
                importUtils.updateLongJobProgress(longJobId, totalCount, currentCount);
            } catch (Exception e) {
                currentCount++;
                System.out.println("Import failed (adding new shopify product) on ebay item id" + ebayItem.itemId());
                System.out.println(e.getMessage());
                failedItemsReport.put(ebayItem.itemId(), e.getMessage());
                importUtils.updateLongJobProgress(longJobId, totalCount, currentCount);

            }
        }

        for (GetItemResponse.Item ebayItem : dedupResult.editDistUnder30()) {
            try {
                if (Thread.currentThread().isInterrupted()) break;
                importUtils.addNewShopifyProductAsPotentialDuplicate(ebayItem, ebayToken, storeName, shopifyToken, userId);
                successfullyImportedCount++;
                currentCount++;
                System.out.println(successfullyImportedCount + " products imported...");
                importUtils.updateLongJobProgress(longJobId, totalCount, currentCount);
            } catch (Exception e) {
                currentCount++;
                System.out.println("Import failed (adding new shopify product as potential dup) on ebay item id" + ebayItem.itemId());
                System.out.println(e.getMessage());
                failedItemsReport.put(ebayItem.itemId(), e.getMessage());
                importUtils.updateLongJobProgress(longJobId, totalCount, currentCount);
            }
        }

        for (Map.Entry<GetItemResponse.Item, ProductsResponse.Product> entry : dedupResult.identical().entrySet()) {
            GetItemResponse.Item ebayItem = entry.getKey();
            ProductsResponse.Product shopifyProduct = entry.getValue();
            try {
                if (Thread.currentThread().isInterrupted()) break;
                importUtils.updateShopifyProduct(ebayItem, ebayToken, shopifyProduct, storeName, shopifyToken, userId);
                successfullyImportedCount++;
                currentCount++;
                System.out.println(successfullyImportedCount + " products imported...");
                importUtils.updateLongJobProgress(longJobId, totalCount, currentCount);
            } catch (Exception e) {
                currentCount++;
                System.out.println("Import failed (updating shopify product) on ebay item id" + ebayItem.itemId());
                System.out.println(e.getMessage());
                failedItemsReport.put(ebayItem.itemId(), e.getMessage());
                importUtils.updateLongJobProgress(longJobId, totalCount, currentCount);
            }
        }

        if (longJobId != null) {
            LongJob longJob = longJobRespository.findById(longJobId).get();
            if (!failedItemsReport.isEmpty()) {
                longJob.setStatus(LongJobStatus.FAILED);
                longJobRespository.save(longJob);
            } else {
                UserInfo userInfo = userInfoRepository.findById(userId).get();
                userInfo.setInitiated(true);
                userInfoRepository.save(userInfo);
                longJob.setStatus(LongJobStatus.COMPLETED);
                longJobRespository.save(longJob);
            }
        }

    }

}
