package com.jobhunt.saas.subscription.application.dto;

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
public class TenantSubscriptionResponseDto {
    private Long id;
    private String tenantName;
    private String planName;
    private BigDecimal amount;
    private int durationInDays;
    private LocalDateTime startDate;
    private LocalDateTime expireDate;
    private String status;
}
