package com.aegis.saas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RazorpayOrderRequest {

    @NotNull(message = "Target plan ID is required.")
    private Long targetPlanId;

    @NotBlank(message = "Billing interval is required.")
    private String billingInterval;
}
