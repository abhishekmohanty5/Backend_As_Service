package com.jobhunt.saas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockPaymentResponse {
    private boolean success;
    private String transactionId;
    private String message;
    private String status; // "COMPLETED" | "FAILED"
}
