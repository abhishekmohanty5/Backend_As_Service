package com.aegis.saas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayVerifyResponse {
    private boolean verified;
    private String message;
    private String orderId;
    private String paymentId;
    private String signature;
    private TenantSubscriptionResponseDto upgradedSubscription;
}
