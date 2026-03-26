package com.jobhunt.saas.dto;

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
    /** Transaction ID returned by the mock payment endpoint */
    private String transactionId;
}
