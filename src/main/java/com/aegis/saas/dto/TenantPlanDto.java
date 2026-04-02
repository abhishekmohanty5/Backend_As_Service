package com.aegis.saas.dto;

import com.aegis.saas.entity.BillingCycle;
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
