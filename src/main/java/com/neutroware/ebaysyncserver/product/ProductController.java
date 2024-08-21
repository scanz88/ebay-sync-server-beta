package com.neutroware.ebaysyncserver.product;

import com.neutroware.ebaysyncserver.ebay.EbayService;
import com.neutroware.ebaysyncserver.ebay.api.getitem.GetItem;
import com.neutroware.ebaysyncserver.ebay.api.getsellerlist.GetSellerList;
import com.neutroware.ebaysyncserver.encryption.EncryptionService;
import com.neutroware.ebaysyncserver.longjob.LongJob;
import com.neutroware.ebaysyncserver.longjob.LongJobRespository;
import com.neutroware.ebaysyncserver.longjob.LongJobStatus;
import com.neutroware.ebaysyncserver.longjob.LongJobType;
import com.neutroware.ebaysyncserver.shopify.ShopifyService;
import com.neutroware.ebaysyncserver.shopify.api.query.products.Products;
import com.neutroware.ebaysyncserver.userinfo.UserInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(
        origins = "*"
)
@RequestMapping("product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;
    private final Importer importer;
    private final GetItem getItem;
    private final UserInfoRepository userInfoRepository;
    private final EbayService ebayService;
    private final ShopifyService shopifyService;
    private final GetSellerList getSellerList;
    private final Products products;
    private final ImportUtils importUtils;
    private final LongJobRespository longJobRespository;
    private final ProductService productService;
    private final EncryptionService encryptionService;


    @GetMapping
    public List<ProductResponse> list(Authentication currentUser) {
        return productService.getProductsByUserId(currentUser.getName());
    }

    @GetMapping
    @RequestMapping("{id}")
    public Product get(@PathVariable Long id) {
        return productRepository.getOne(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody final Product product) {
        return productRepository.saveAndFlush(product);
    }

    @GetMapping("/bulk-import")
    public void bulkImport(Authentication currentUser) throws Exception {
        importer.bulkImportFromEbayToShopify(currentUser.getName());
    }

    @GetMapping("/test")
    public void test() throws Exception {
    //importer.restoreFailedMedia("7a0b4d46-0209-4c4a-9e82-84c151b3a675");
        //importer.findDuplicateShopifyProducts("7a0b4d46-0209-4c4a-9e82-84c151b3a675");
        importer.auxBulkImportFromEbayToShopify("52a04c17-9567-4f08-83f5-680ea57e1a94");
    }
}
