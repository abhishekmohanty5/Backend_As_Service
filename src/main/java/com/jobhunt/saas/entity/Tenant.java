package com.jobhunt.saas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private LocalDateTime createdAt;

    @Column(nullable = false, unique = true, updatable = false)
    private String clientId;

    @Column(nullable = false, updatable = false)
    private String clientSecret;

    @Column(nullable = false)
    private Long apiCallCount = 0L;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.clientId == null) {
            this.clientId = "sb_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        if (this.clientSecret == null) {
            this.clientSecret = "sk_" + UUID.randomUUID().toString().replace("-", "");
        }
        if (this.apiCallCount == null) {
            this.apiCallCount = 0L;
        }
    }

}
