package com.neutroware.ebaysyncserver.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySyncedFalseAndUserId(String userId);

    Optional<Product> findByShopifyProductId(String shopifyProductId);

    Optional<Product> findByEbayItemId(String ebayItemId);

    Optional<Product> findByTitle(String title);

    List<Product> findByUserId(String userId);
}
