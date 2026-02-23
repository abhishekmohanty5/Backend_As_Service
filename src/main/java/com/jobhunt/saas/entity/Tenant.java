package com.jobhunt.saas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

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

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TenantSubscription> subscriptions = new ArrayList<>();

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

    @com.fasterxml.jackson.annotation.JsonIgnore
    public TenantSubscription getActiveSubscription() {
        if (subscriptions == null || subscriptions.isEmpty())
            return null;
        return subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElse(subscriptions.get(subscriptions.size() - 1));
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public Plan getPlan() {
        TenantSubscription sub = getActiveSubscription();
        return sub != null ? sub.getPlan() : null;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public SubscriptionStatus getStatus() {
        TenantSubscription sub = getActiveSubscription();
        return sub != null ? sub.getStatus() : null;
    }

}
