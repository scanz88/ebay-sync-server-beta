package com.neutroware.ebaysyncserver.ebay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.neutroware.ebaysyncserver.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ebay_transaction")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EbayTransaction {

    @Id
    private String ebayTransactionId;
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    private LocalDateTime paidTime;
}
