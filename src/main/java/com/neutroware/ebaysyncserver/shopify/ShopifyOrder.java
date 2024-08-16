package com.neutroware.ebaysyncserver.shopify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.neutroware.ebaysyncserver.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "shopify_order")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ShopifyOrder {

    @Id
    private String shopifyOrderId;
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    private OffsetDateTime createdAt;
}
