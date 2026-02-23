package com.jobhunt.saas.dto;

import com.jobhunt.saas.entity.BillingCycle;
import com.jobhunt.saas.entity.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class UserSubscriptionDto {
    private Long id;
    private Long userId;
    private Long tenantPlanId;
    private String username;
    private String subscriptionName;
    private BigDecimal amount;
    private BillingCycle billingCycle;
    private LocalDate startDate;
    private LocalDate nextBillingDate;
    private SubscriptionStatus status;
    private String notes;
}
