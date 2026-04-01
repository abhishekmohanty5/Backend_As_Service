package com.jobhunt.saas.subscription.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionResponse {
    private String message;
    private Long id;
    private Long planId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
