package com.aegis.saas.dto;

import lombok.Data;

@Data
public class PublicSubscriptionRequest {
    private Long userId;
    private Long tenantPlanId;
    private String notes;
}
