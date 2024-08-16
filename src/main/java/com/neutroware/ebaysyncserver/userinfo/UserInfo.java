package com.neutroware.ebaysyncserver.userinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.joda.time.LocalDateTime;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_info")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserInfo {
    @Id
    private String userId;
    private String shopifyToken;
    private String ebayToken;
    private String ebayRefreshToken;
    private Boolean initiated;
    private Date ebayTokenExp;
    private Date ebayRefreshTokenExp;
    private String shopifyStoreName;
    private String shopifyWebhookSignature;

//    @LastModifiedDate
//    @Column(insertable = false)
//    private LocalDateTime lastModifiedDate;
}
