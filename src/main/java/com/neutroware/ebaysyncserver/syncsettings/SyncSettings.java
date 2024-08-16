package com.neutroware.ebaysyncserver.syncsettings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "sync_settings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SyncSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long syncSettingsId;
    private Boolean sync;
    private Boolean applyMarkup;
    private Float markupPercent;
    private Boolean applyMarkdown;
    private Float markdownPercent;
    private String userId;
    //Add shopify sales channel list to sync to (maybe add new entity for sales chanel to have one to many mapping between user/syncsettings and sales channels

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;
}
