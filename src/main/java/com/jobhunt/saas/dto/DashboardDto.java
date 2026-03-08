package com.jobhunt.saas.dto;

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
public class DashboardDto {

    // Tenant Info
    private String tenantName;
    private String currentPlan;
    private BigDecimal planPrice;
    private String status;
    private LocalDateTime memberSince;

    // Plan Expiry
    private LocalDateTime planExpiryDate;
    private long daysRemaining;

    // API Credentials
    private String clientId;
    private String clientSecret;

    // Usage
    private Long apiCallCount;
    private Long apiCallLimit;

    // Services Enabled
    private boolean authServiceEnabled;
    private boolean subscriptionServiceEnabled;
    private boolean emailNotificationsEnabled;
    private boolean schedulerEnabled;

    // Reference Module Stats
    private long totalUserSubscriptions;
    private long activeUserSubscriptions;
}
