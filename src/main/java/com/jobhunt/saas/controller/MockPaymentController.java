package com.jobhunt.saas.controller;

import com.jobhunt.saas.dto.AppResponse;
import com.jobhunt.saas.dto.MockPaymentRequest;
import com.jobhunt.saas.dto.MockPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mock Payment Gateway Controller
 *
 * Simulates the payment processing step that would normally go through
 * a third-party gateway (Razorpay / Stripe).
 *
 * Industry-standard flow:
 *   1. Frontend calls POST /pay  → receives transactionId
 *   2. Frontend calls POST /upgrade with transactionId → subscription activated
 *
 * Test cards that trigger failure (card last 4 digits):
 *   0002 → insufficient funds
 *   9999 → card declined
 *   All other valid-length cards → success
 *
 * Test UPI:
 *   fail@upi   → payment failed
 *   Any other @<bank> format → success
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant-admin/engine-subscription")
public class MockPaymentController {

    @PostMapping("/pay")
    public ResponseEntity<AppResponse<MockPaymentResponse>> processPayment(
            @RequestBody MockPaymentRequest request) {

        // Simulate realistic processing delay (handled client-side via animation;
        // backend just validates and returns immediately for API responsiveness)

        MockPaymentResponse result;

        try {
            result = validate(request);
        } catch (IllegalArgumentException e) {
            result = MockPaymentResponse.builder()
                    .success(false)
                    .transactionId(null)
                    .status("FAILED")
                    .message(e.getMessage())
                    .build();
        }

        if (result.isSuccess()) {
            log.info("[MockPayment] SUCCESS | method={} plan={} txn={}",
                    request.getPaymentMethod(), request.getPlanId(), result.getTransactionId());
        } else {
            log.warn("[MockPayment] FAILED | method={} plan={} reason={}",
                    request.getPaymentMethod(), request.getPlanId(), result.getMessage());
        }

        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.PAYMENT_REQUIRED;
        AppResponse<MockPaymentResponse> response = new AppResponse<>(
                result.getMessage(), result, status.value(), LocalDateTime.now());
        return ResponseEntity.status(status).body(response);
    }

    // -----------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------

    private MockPaymentResponse validate(MockPaymentRequest req) {
        if (req.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required.");
        }

        return switch (req.getPaymentMethod().toUpperCase()) {
            case "CARD" -> validateCard(req);
            case "UPI"  -> validateUpi(req);
            default     -> throw new IllegalArgumentException("Unsupported payment method: " + req.getPaymentMethod());
        };
    }

    private MockPaymentResponse validateCard(MockPaymentRequest req) {
        String raw = req.getCardNumber() == null ? "" : req.getCardNumber().replaceAll("\\s", "");

        if (raw.length() < 13 || raw.length() > 19) {
            throw new IllegalArgumentException("Invalid card number length.");
        }
        if (!raw.matches("\\d+")) {
            throw new IllegalArgumentException("Card number must contain only digits.");
        }
        if (req.getCardCvv() == null || !req.getCardCvv().matches("\\d{3,4}")) {
            throw new IllegalArgumentException("Invalid CVV.");
        }
        if (req.getCardExpiry() == null || !req.getCardExpiry().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new IllegalArgumentException("Invalid expiry. Use MM/YY format.");
        }

        // Simulate specific decline scenarios using last 4 digits
        String last4 = raw.substring(raw.length() - 4);
        if ("0002".equals(last4)) {
            return failed("Your card has insufficient funds.");
        }
        if ("9999".equals(last4)) {
            return failed("Your card was declined. Contact your bank.");
        }

        return success("Payment successful. Plan activated.");
    }

    private MockPaymentResponse validateUpi(MockPaymentRequest req) {
        String upi = req.getUpiId() == null ? "" : req.getUpiId().trim();

        // Basic UPI format: something@bankname
        if (!upi.matches("^[a-zA-Z0-9.\\-_+]+@[a-zA-Z]{3,}$")) {
            throw new IllegalArgumentException("Invalid UPI ID. Expected format: name@bank");
        }

        if ("fail@upi".equalsIgnoreCase(upi)) {
            return failed("UPI payment was rejected by your bank.");
        }

        return success("UPI payment verified. Plan activated.");
    }

    private MockPaymentResponse success(String message) {
        return MockPaymentResponse.builder()
                .success(true)
                .transactionId("TXN-" + UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 16))
                .status("COMPLETED")
                .message(message)
                .build();
    }

    private MockPaymentResponse failed(String message) {
        return MockPaymentResponse.builder()
                .success(false)
                .transactionId(null)
                .status("FAILED")
                .message(message)
                .build();
    }
}
