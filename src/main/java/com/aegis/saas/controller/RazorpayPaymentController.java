package com.aegis.saas.controller;

import com.aegis.saas.dto.AppResponse;
import com.aegis.saas.dto.RazorpayOrderRequest;
import com.aegis.saas.dto.RazorpayOrderResponse;
import com.aegis.saas.dto.RazorpayVerifyRequest;
import com.aegis.saas.dto.RazorpayVerifyResponse;
import com.aegis.saas.service.RazorpayPaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "Tenant Admin - Razorpay Payments", description = "Create Razorpay test orders and verify successful payments.")
@RestController
@RequestMapping("/api/v1/tenant-admin/billing/razorpay")
@RequiredArgsConstructor
public class RazorpayPaymentController {

    private final RazorpayPaymentService razorpayPaymentService;

    @PostMapping("/order")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<AppResponse<RazorpayOrderResponse>> createOrder(@Valid @RequestBody RazorpayOrderRequest request) {
        RazorpayOrderResponse data = razorpayPaymentService.createOrder(request);
        AppResponse<RazorpayOrderResponse> response = AppResponse.<RazorpayOrderResponse>builder()
                .message("Razorpay order created successfully")
                .data(data)
                .status(HttpStatus.CREATED.value())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<AppResponse<RazorpayVerifyResponse>> verifyPayment(@Valid @RequestBody RazorpayVerifyRequest request) {
        RazorpayVerifyResponse data = razorpayPaymentService.verifyAndUpgrade(request);
        AppResponse<RazorpayVerifyResponse> response = AppResponse.<RazorpayVerifyResponse>builder()
                .message("Razorpay payment verified successfully")
                .data(data)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
