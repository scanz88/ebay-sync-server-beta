package com.neutroware.ebaysyncserver.activity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "activity")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;
    @Enumerated(EnumType.STRING)
    private ActivityFlow flow;
    private String description;
    @Enumerated(EnumType.STRING)
    private ActivityStatus status;
    private String userId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;
}
