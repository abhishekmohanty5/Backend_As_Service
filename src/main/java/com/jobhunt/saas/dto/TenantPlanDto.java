package com.jobhunt.saas.dto;

import com.jobhunt.saas.entity.BillingCycle;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TenantPlanDto {
    private String name;
    private String description;
    private BigDecimal price;
    private BillingCycle billingCycle;
    private String features;
}
