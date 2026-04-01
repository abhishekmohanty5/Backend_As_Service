package com.jobhunt.saas.tenantplan.application.dto;

import com.jobhunt.saas.shared.domain.model.BillingCycle;
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
