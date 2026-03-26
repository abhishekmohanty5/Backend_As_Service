package com.jobhunt.saas.dto;

import lombok.Data;

/**
 * Request body for initiating a mock payment.
 * Either card fields OR upiId must be provided (based on paymentMethod).
 */
@Data
public class MockPaymentRequest {

    /** "CARD" or "UPI" */
    private String paymentMethod;

    // ---- Card fields (required when paymentMethod == "CARD") ----
    private String cardNumber;
    private String cardExpiry;   // MM/YY
    private String cardCvv;
    private String cardHolderName;

    // ---- UPI field (required when paymentMethod == "UPI") ----
    private String upiId;

    // ---- Common ----
    /** Amount in INR (informational — used for logging / UX) */
    private Double amount;
    /** Target plan ID so the backend can validate before processing */
    private Long planId;
}
