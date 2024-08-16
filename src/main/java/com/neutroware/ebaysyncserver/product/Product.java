package com.neutroware.ebaysyncserver.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.neutroware.ebaysyncserver.ebay.EbayTransaction;
import com.neutroware.ebaysyncserver.shopify.ShopifyOrder;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "product")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String shopifyProductId;
    private String ebayItemId;
    private Integer ebayQuantity;
    private Integer shopifyQuantity;
    private String title;
    private String userId;
    private String shopifyVariantId;
    private Float weight;
    private String weightUnit;
    private Float ebayPrice;
    private Float shopifyPrice;
    private String shopifyInventoryItemId;
    private String shopifyInventoryLocationId;
    private Boolean synced;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EbayTransaction> ebayTransactions;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShopifyOrder> shopifyOrders;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;
}
