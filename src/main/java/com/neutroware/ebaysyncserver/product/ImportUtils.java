package com.neutroware.ebaysyncserver.product;

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
import com.neutroware.ebaysyncserver.shopify.api.mutation.productupdate.ProductUpdate;
import com.neutroware.ebaysyncserver.shopify.api.mutation.productupdate.ProductUpdateArgs;
import com.neutroware.ebaysyncserver.shopify.api.mutation.productvariantupdate.ProductVariantUpdate;
import com.neutroware.ebaysyncserver.shopify.api.mutation.productvariantupdate.ProductVariantUpdateArgs;
import com.neutroware.ebaysyncserver.shopify.api.mutation.publishablePublish.PublishablePublish;
import com.neutroware.ebaysyncserver.shopify.api.mutation.publishablePublish.PublishablePublishArgs;
import com.neutroware.ebaysyncserver.shopify.api.query.products.Products;
import com.neutroware.ebaysyncserver.shopify.api.query.products.ProductsResponse;
import com.neutroware.ebaysyncserver.shopify.api.query.publications.Publications;
import com.neutroware.ebaysyncserver.syncsettings.SyncSettings;
import com.neutroware.ebaysyncserver.syncsettings.SyncSettingsRepository;
import com.neutroware.ebaysyncserver.userinfo.UserInfoRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportUtils {
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
    private final GetItem getItem;
    private final ProductUpdate productUpdate;
    private final LongJobRespository longJobRespository;
    private final EntityManager entityManager;
    private final SyncSettingsRepository syncSettingsRepository;

    public static Set<String> rootCategories = new HashSet<>();
    public static final List<String> CUSTOM_DECADE_TAGS = Collections.unmodifiableList(
            new ArrayList<String>() {
                {
                    add("1800's");add("1870's");add("1880's");add("1890's");add("1900's");add("1910's");
                    add("1920's");add("1930's");add("1940's");add("1950's");add("1960's");
                    add("1970's");add("1980's");add("1990's");
                }
            }
    );
    public static final List<String> CUSTOM_MISC_TAGS = Collections.unmodifiableList(
            new ArrayList<String>() {
                {
                    add("Vintage");add("MCM");add("WWII");add("Advertising");add("Beer");add("Adult");
                }
            }
    );
    public static final List<String> ITEM_SPECIFICS = Collections.unmodifiableList(
            new ArrayList<String>() {
                {
                    add("Author");add("Style");add("Artist");
                }
            }
    );

    public List<String> buildTags(GetItemResponse.Item item) {
        List<String> tags = new ArrayList<>();
        CUSTOM_DECADE_TAGS.forEach(tag -> {
            if (item.title().contains(tag.substring(0, 3))) {
                tags.add(tag);
            }
        });
        CUSTOM_MISC_TAGS.forEach(tag -> {
            if (item.title().toLowerCase().contains(tag.toLowerCase())) {
                tags.add(tag);
            }
        });
        try {
            if (item.itemSpecifics() != null) {
                ITEM_SPECIFICS.forEach(specific -> {
                    item.itemSpecifics().nameValueList().forEach(nameValuePair -> {
                        if (nameValuePair.name().equalsIgnoreCase(specific)) {
                            tags.addAll(nameValuePair.value());
                        }
                    });
                });
            }
        } catch (Exception e) {
            System.out.println("Failed to parse item specifics " + e.getMessage());
        }
        List<String> tagsFromCategories = List.of(item.primaryCategory().categoryName().split(":"));
        rootCategories.add(tagsFromCategories.get(0)); //logging purposes
        tags.addAll(tagsFromCategories);
        return tags;

    }

    public List<ProductCreateArgs.Media> buildMediaList(GetItemResponse.Item item) {
        List<ProductCreateArgs.Media> list = new ArrayList<>();
        item.pictureDetails().pictureURL().forEach(p -> {
            list.add(new ProductCreateArgs.Media("alt", "IMAGE", p));
        });
        return list;
    }

    public ProductVariantUpdateArgs.InventoryItem buildInventoryItem(GetItemResponse.Item item) {
        if (item.shippingDetails() != null && item.shippingDetails().calculatedShippingRate() != null) {
            var weightMajor = item.shippingDetails().calculatedShippingRate().weightMajor();
            var weightMinor = item.shippingDetails().calculatedShippingRate().weightMinor();

            if (weightMajor.value() == 0) {
                return new ProductVariantUpdateArgs.InventoryItem(
                        new ProductVariantUpdateArgs.Measurement(
                                new ProductVariantUpdateArgs.Weight(
                                        "OUNCES",
                                        weightMinor.value()
                                )
                        ),

                        true,
                        true
                );
            } else {
                return new ProductVariantUpdateArgs.InventoryItem(
                        new ProductVariantUpdateArgs.Measurement(
                                new ProductVariantUpdateArgs.Weight(
                                        "POUNDS",
                                        weightMajor.value() + weightMinor.value()/16 //divide by 16 to convert oz to lbs
                                )
                        ),
                        true,
                        true
                );
            }

        } else {
            return new ProductVariantUpdateArgs.InventoryItem(
                    new ProductVariantUpdateArgs.Measurement(
                            new ProductVariantUpdateArgs.Weight(
                                    "POUNDS",
                                    0f
                            )
                    ),
                    true,
                    false
            );
        }
    }

    public DeduplicationResult deduplicate(
            List<GetItemResponse.Item> allEbayItems,
            List<ProductsResponse.Product> allShopifyProducts
    ) {
        //Initialize
        DeduplicationResult result = new DeduplicationResult(
                new HashMap<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        for (GetItemResponse.Item item : allEbayItems) {
            boolean isIdenticalFound = false;
            boolean isEditDistLessThan30Found = false;

            for (ProductsResponse.Product shopifyProd : allShopifyProducts) {
                if (shopifyProd.title().equalsIgnoreCase(item.title())) {
                   result.identical().put(item, shopifyProd);
                    isIdenticalFound = true;
                    break; // Break inner loop if identical is found
                } else if (StringUtils.getLevenshteinDistance(shopifyProd.title().toLowerCase(), item.title().toLowerCase()) < 30) {
                    isEditDistLessThan30Found = true;
                }
            }

            if (!isIdenticalFound) {
                if (isEditDistLessThan30Found) {
                   result.editDistUnder30().add(item);
                } else {
                    result.editDistOver30().add(item);
                }
            }
        }
        // Print sizes for debugging
        System.out.println("Identical size = " + result.identical().size());
        System.out.println("editDistOverThan30 size = " + result.editDistOver30().size());
        System.out.println("editDistUnder30 size = " + result.editDistUnder30().size());
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addNewShopifyProduct(
            GetItemResponse.Item ebayItem,
            String ebayToken,
            String storeName,
            String shopifyToken,
            String userId
    ) {
        Product product = initializeProduct(ebayItem, userId);
        GetItemResponse.Item itemWithSpecifics = fetchEbayItemWithSpecifics(ebayItem, ebayToken);

        var media = buildMediaList(ebayItem);
        List<String> tags = buildTags(
                itemWithSpecifics != null ?
                        itemWithSpecifics : ebayItem
        );
        ProductCreateArgs productCreateArgs = new ProductCreateArgs(
                new ProductCreateArgs.ProductInput(
                        ebayItem.description(),
                        tags,
                        ebayItem.title()
                ),
                media
        );
        var productCreateResult = productCreate.createProduct(storeName, shopifyToken, productCreateArgs);
        var productId = productCreateResult.product().id();
        var variantId = productCreateResult.product().variants().edges().get(0).node().id();
        var inventoryItemId = productCreateResult.product().variants().edges().get(0).node().inventoryItem().id();
        var locationId = productCreateResult.product().variants().edges().get(0).node().inventoryItem().inventoryLevels().edges().get(0).node().location().id();
        product.setShopifyProductId(productId);
        product.setShopifyVariantId(variantId);
        product.setShopifyInventoryItemId(inventoryItemId);
        product.setShopifyInventoryLocationId(locationId);
        product = productRepository.save(product);

        ProductVariantUpdateArgs.InventoryItem inventoryItem = buildInventoryItem(ebayItem);
        Float ebayPrice = ebayItem.sellingStatus().currentPrice().value();
        Float shopifyPrice = adjustPrice(userId, ebayPrice);
        ProductVariantUpdateArgs productVariantUpdateArgs = new ProductVariantUpdateArgs(
                new ProductVariantUpdateArgs.ProductVariantInput(
                        variantId,
                        shopifyPrice.toString(),
                        inventoryItem
                )
        );
        var productVariantUpdateResult = productVariantUpdate.updateVariant(storeName, shopifyToken, productVariantUpdateArgs);
        product.setEbayPrice(ebayPrice);
        product.setShopifyPrice(shopifyPrice);
        product.setWeight(inventoryItem.measurement().weight().value());
        product.setWeightUnit(inventoryItem.measurement().weight().unit());
        product = productRepository.save(product);

        Integer ebayQuantity = ebayItem.quantity() - ebayItem.sellingStatus().quantitySold();
        InventoryAdjustQuantitiesArgs inventoryArgs = new InventoryAdjustQuantitiesArgs(
                new InventoryAdjustQuantitiesArgs.InventoryAdjustQuantitiesInput(
                        "available",
                        "correction",
                        new ArrayList<>(List.of(
                                new InventoryAdjustQuantitiesArgs.Change(
                                        ebayQuantity,
                                        inventoryItemId,
                                        locationId
                                )
                        ))
                )
        );
        var inventoryResult = inventoryAdjustQuantities.adjustQuantities(storeName, shopifyToken, inventoryArgs);
        product.setEbayQuantity(ebayQuantity);
        product.setShopifyQuantity(ebayQuantity);
        product = productRepository.save(product);

        PublishablePublishArgs publishablePublishArgs = new PublishablePublishArgs(
                productId,
                new ArrayList<>(List.of(
                        new PublishablePublishArgs.PublicationInput(
                                "gid://shopify/Publication/112832184631"
                        )
                ))
        );
        var publishResult = publishablePublish.publishResource(storeName, shopifyToken, publishablePublishArgs);
        product.setSynced(true);
        productRepository.save(product);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addNewShopifyProductAsPotentialDuplicate(
            GetItemResponse.Item ebayItem,
            String ebayToken,
            String storeName,
            String shopifyToken,
            String userId
    ) {
        Product product = initializeProduct(ebayItem, userId);
        GetItemResponse.Item itemWithSpecifics = fetchEbayItemWithSpecifics(ebayItem, ebayToken);

        var media = buildMediaList(ebayItem);
        List<String> tags = buildTags(
                itemWithSpecifics != null ?
                        itemWithSpecifics : ebayItem
        );
        tags.add("Potential duplicate");
        ProductCreateArgs productCreateArgs = new ProductCreateArgs(
                new ProductCreateArgs.ProductInput(
                        ebayItem.description(),
                        tags,
                        ebayItem.title()
                ),
                media
        );
        var productCreateResult = productCreate.createProduct(storeName, shopifyToken, productCreateArgs);
        var productId = productCreateResult.product().id();
        var variantId = productCreateResult.product().variants().edges().get(0).node().id();
        var inventoryItemId = productCreateResult.product().variants().edges().get(0).node().inventoryItem().id();
        var locationId = productCreateResult.product().variants().edges().get(0).node().inventoryItem().inventoryLevels().edges().get(0).node().location().id();
        product.setShopifyProductId(productId);
        product.setShopifyVariantId(variantId);
        product.setShopifyInventoryItemId(inventoryItemId);
        product.setShopifyInventoryLocationId(locationId);
        product = productRepository.save(product);

        ProductVariantUpdateArgs.InventoryItem inventoryItem = buildInventoryItem(ebayItem);
        Float ebayPrice = ebayItem.sellingStatus().currentPrice().value();
        Float shopifyPrice = adjustPrice(userId, ebayPrice);
        ProductVariantUpdateArgs productVariantUpdateArgs = new ProductVariantUpdateArgs(
                new ProductVariantUpdateArgs.ProductVariantInput(
                        variantId,
                        shopifyPrice.toString(),
                        inventoryItem
                )
        );
        var productVariantUpdateResult = productVariantUpdate.updateVariant(storeName, shopifyToken, productVariantUpdateArgs);
        product.setEbayPrice(ebayPrice);
        product.setShopifyPrice(shopifyPrice);
        product.setWeight(inventoryItem.measurement().weight().value());
        product.setWeightUnit(inventoryItem.measurement().weight().unit());
        product = productRepository.save(product);

        Integer ebayQuantity = ebayItem.quantity() - ebayItem.sellingStatus().quantitySold();
        InventoryAdjustQuantitiesArgs inventoryArgs = new InventoryAdjustQuantitiesArgs(
                new InventoryAdjustQuantitiesArgs.InventoryAdjustQuantitiesInput(
                        "available",
                        "correction",
                        new ArrayList<>(List.of(
                                new InventoryAdjustQuantitiesArgs.Change(
                                        ebayQuantity,
                                        inventoryItemId,
                                        locationId
                                )
                        ))
                )
        );
        var inventoryResult = inventoryAdjustQuantities.adjustQuantities(storeName, shopifyToken, inventoryArgs);
        product.setEbayQuantity(ebayQuantity);
        product.setShopifyQuantity(ebayQuantity);
        product = productRepository.save(product);

        PublishablePublishArgs publishablePublishArgs = new PublishablePublishArgs(
                productId,
                new ArrayList<>(List.of(
                        new PublishablePublishArgs.PublicationInput(
                                "gid://shopify/Publication/112832184631"
                        )
                ))
        );
        var publishResult = publishablePublish.publishResource(storeName, shopifyToken, publishablePublishArgs);
        product.setSynced(true);
        productRepository.save(product);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateShopifyProduct(
            GetItemResponse.Item ebayItem,
            String ebayToken,
            ProductsResponse.Product shopifyProduct,
            String storeName,
            String shopifyToken,
            String userId
    ) {
        System.out.println("in updateShopifyProduct method !!!!!!");
        Product product = initializeProduct(ebayItem, userId);
        GetItemResponse.Item itemWithSpecifics = fetchEbayItemWithSpecifics(ebayItem, ebayToken);

        List<String> tags = buildTags(
                itemWithSpecifics != null ?
                        itemWithSpecifics : ebayItem
        );
        tags.addAll(shopifyProduct.tags());
        ProductUpdateArgs productUpdateArgsArgs = new ProductUpdateArgs(
                new ProductUpdateArgs.ProductInput(
                        shopifyProduct.id(),
                        tags
                )
        );
        var productUpdateResult = productUpdate.updateProduct(storeName, shopifyToken, productUpdateArgsArgs);

        var productId = productUpdateResult.product().id();
        var variantId = productUpdateResult.product().variants().edges().get(0).node().id();
        var inventoryItemId = productUpdateResult.product().variants().edges().get(0).node().inventoryItem().id();
        var locationId = productUpdateResult.product().variants().edges().get(0).node().inventoryItem().inventoryLevels().edges().get(0).node().location().id();
        product.setShopifyProductId(productId);
        product.setShopifyVariantId(variantId);
        product.setShopifyInventoryItemId(inventoryItemId);
        product.setShopifyInventoryLocationId(locationId);
        product = productRepository.save(product);

        ProductVariantUpdateArgs.InventoryItem inventoryItem = buildInventoryItem(ebayItem);
        Float ebayPrice = ebayItem.sellingStatus().currentPrice().value();
        Float shopifyPrice = adjustPrice(userId, ebayPrice);
        ProductVariantUpdateArgs productVariantUpdateArgs = new ProductVariantUpdateArgs(
                new ProductVariantUpdateArgs.ProductVariantInput(
                        variantId,
                        shopifyPrice.toString(),
                        inventoryItem
                )
        );
        var productVariantUpdateResult = productVariantUpdate.updateVariant(storeName, shopifyToken, productVariantUpdateArgs);
        product.setEbayPrice(ebayPrice);
        product.setShopifyPrice(shopifyPrice);
        product.setWeight(inventoryItem.measurement().weight().value());
        product.setWeightUnit(inventoryItem.measurement().weight().unit());
        product = productRepository.save(product);

        Integer ebayQuantity = ebayItem.quantity() - ebayItem.sellingStatus().quantitySold();
        InventoryAdjustQuantitiesArgs inventoryArgs = new InventoryAdjustQuantitiesArgs(
                new InventoryAdjustQuantitiesArgs.InventoryAdjustQuantitiesInput(
                        "available",
                        "correction",
                        new ArrayList<>(List.of(
                                new InventoryAdjustQuantitiesArgs.Change(
                                        ebayQuantity - shopifyProduct.totalInventory(),
                                        inventoryItemId,
                                        locationId
                                )
                        ))
                )
        );
        var inventoryResult = inventoryAdjustQuantities.adjustQuantities(storeName, shopifyToken, inventoryArgs);
        product.setEbayQuantity(ebayQuantity);
        product.setShopifyQuantity(ebayQuantity);
        product = productRepository.save(product);

        PublishablePublishArgs publishablePublishArgs = new PublishablePublishArgs(
                productId,
                new ArrayList<>(List.of(
                        new PublishablePublishArgs.PublicationInput(
                                "gid://shopify/Publication/112832184631"
                        )
                ))
        );
        var publishResult = publishablePublish.publishResource(storeName, shopifyToken, publishablePublishArgs);
        product.setSynced(true);
        productRepository.save(product);
    }

    //TODO: add refresh token call here since long running process
    public GetItemResponse.Item fetchEbayItemWithSpecifics(GetItemResponse.Item ebayItem, String ebayToken) {
        try {
            return getItem.getItem(ebayToken, ebayItem.itemId()).item();
        } catch (Exception e) {
            System.out.println("Failed to fetch ebay item using getItem (for specifics): " + e.getMessage());
            return null;
        }
    }

    private Product initializeProduct(GetItemResponse.Item ebayItem, String userId) {
        Product product = new Product();
        product.setEbayItemId(ebayItem.itemId());
        product.setUserId(userId);
        product.setTitle(ebayItem.title());
        product.setSynced(false);
        return productRepository.save(product);
    }

    private Float adjustPrice(String userId, Float price) {
        Float adjustedPrice = price;
        SyncSettings syncSettings = syncSettingsRepository.findByUserId(userId).get();
        //TODO: Make sure in frontend that either only markdown or markup can be applied but not both
        // or do a combined calculation
        if (syncSettings.getApplyMarkup()) {
            adjustedPrice = price * (1 + (syncSettings.getMarkupPercent()/100));
        }
        if (syncSettings.getApplyMarkdown()) {
            adjustedPrice = price * (1 - (syncSettings.getMarkdownPercent()/100));
        }
        return adjustedPrice;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LongJob initializeLongJob(String userId) {
        LongJob longJob = new LongJob();
        longJob.setProgress(0);
        longJob.setStatus(LongJobStatus.IN_PROGRESS);
        longJob.setUserId(userId);
        longJob.setJobType(LongJobType.BULK_IMPORT);
        return longJobRespository.save(longJob);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLongJobProgress(Long longJobId, int totalCount, int currentCount) {
        if (longJobId != null) {
           LongJob runningLongJob = longJobRespository.findById(longJobId).get();
           runningLongJob.setProgress((int) Math.floor((double) currentCount/totalCount * 100));
           longJobRespository.save(runningLongJob);
        }
    }
}
