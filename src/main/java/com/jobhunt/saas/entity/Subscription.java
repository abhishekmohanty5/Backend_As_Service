package com.jobhunt.saas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions",
        indexes = {
                @Index(name = "idx_subscription_user", columnList = "userId")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long tenantId;


    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

}
