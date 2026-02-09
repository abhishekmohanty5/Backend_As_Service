package com.jobhunt.saas.dto;

import com.jobhunt.saas.entity.BillingCycle;
import com.jobhunt.saas.entity.SubscriptionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionDto {
    private Long id;
    private String subscriptionName;
    private SubscriptionCategory category;
    private BigDecimal amount;
    private BillingCycle billingCycle;
    private LocalDate startDate;
    private LocalDate nextBillingDate;
    private String notes;
}
