package com.aegis.saas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnginePlanUpgradeRequest {
    private Long targetPlanId;
    /** "MONTHLY" or "ANNUAL" */
    private String billingInterval;
    /** Razorpay payment ID returned after the checkout signature is verified */
    private String transactionId;
}
