package com.jobhunt.saas.dto;

import com.jobhunt.saas.entity.BillingCycle;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TenantPlanResponseDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BillingCycle billingCycle;
    private String features;
    private boolean active;
}
