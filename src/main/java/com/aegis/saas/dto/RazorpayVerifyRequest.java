package com.aegis.saas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RazorpayVerifyRequest {

    @NotNull(message = "Target plan ID is required.")
    private Long targetPlanId;

    @NotBlank(message = "Billing interval is required.")
    private String billingInterval;

    @NotBlank(message = "Razorpay order ID is required.")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay payment ID is required.")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay signature is required.")
    private String razorpaySignature;
}
