package com.aegis.saas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResponse {
    private String gatewayMode;
    private String keyId;
    private String merchantName;
    private String description;
    private String currency;
    private Long amountInPaise;
    private BigDecimal amount;
    private Long planId;
    private String planName;
    private String billingInterval;
    private String orderId;
    private String receipt;
    private LocalDateTime createdAt;
}
