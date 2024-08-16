package com.neutroware.ebaysyncserver.longjob;

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
@Table(name = "long_job")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LongJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long longJobId;
    private Integer progress; //1-100 percent
    @Enumerated(EnumType.STRING)
    private LongJobStatus status;
    @Enumerated(EnumType.STRING)
    private LongJobType jobType;
    private String userId;
    private String failureMessage;
    private String successMessage;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;
}
