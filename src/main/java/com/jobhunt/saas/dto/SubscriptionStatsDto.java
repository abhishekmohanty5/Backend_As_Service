package com.jobhunt.saas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatsDto {
     private BigDecimal totalMonthlySpend;
    private int activeSubscriptions;

}
