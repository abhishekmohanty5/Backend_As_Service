package com.aegis.saas.controller;

import com.aegis.saas.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.aegis.saas.service.AiService;
import com.aegis.saas.service.TenantPublicApiService;
import com.aegis.saas.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "End Users - BaaS API", description = "Backend-as-a-Service endpoints consumed by tenant's end users (register, login, subscribe, AI features)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class TenantPublicApiController {

    private final AiService aiService;
    private final TenantPublicApiService tenantPublicApiService;

    @GetMapping("/ai/analytics")
    public ResponseEntity<AppResponse<String>> getSubscriptionAnalytics() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Unauthorized API access");
        }

        String analytics = aiService.generateSubscriptionAnalytics(tenantId);

        AppResponse<String> response = AppResponse.<String>builder()
                .message("Successfully generated AI Subscription Analytics")
                .data(analytics)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/ai/generate-plans")
    public ResponseEntity<AppResponse<java.util.List<TenantPlanDto>>> generatePricingPlans(
            @RequestBody AiPlanRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Unauthorized API access");
        }

        try {
            java.util.List<TenantPlanDto> plans = aiService.generatePricingPlans(request.getBusinessDescription());

            AppResponse<java.util.List<TenantPlanDto>> response = AppResponse.<java.util.List<TenantPlanDto>>builder()
                    .message("Successfully generated AI Pricing Plans")
                    .data(plans)
                    .status(HttpStatus.OK.value())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AppResponse<java.util.List<TenantPlanDto>> errorResponse = AppResponse.<java.util.List<TenantPlanDto>>builder()
                    .message("AI Generation failed: " + e.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/ai/predict-churn/{userId}")
    public ResponseEntity<AppResponse<String>> predictChurnRisk(@PathVariable Long userId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Unauthorized API access");
        }

        String prediction = aiService.predictChurnRisk(userId, tenantId);

        AppResponse<String> response = AppResponse.<String>builder()
                .message("Successfully generated AI Churn Prediction")
                .data(prediction)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tenant-plans")
    public ResponseEntity<AppResponse<java.util.List<TenantPlanResponseDto>>> getPublicTenantPlans() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Unauthorized API access");
        }

        java.util.List<TenantPlanResponseDto> dtos = tenantPublicApiService.getPublicTenantPlans(tenantId);

        AppResponse<java.util.List<TenantPlanResponseDto>> response = AppResponse.<java.util.List<TenantPlanResponseDto>>builder()
                .message("Successfully retrieved public Tenant Plans")
                .data(dtos)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/register")
    public ResponseEntity<AppResponse<TenantUserDto>> registerEndUser(@RequestBody EndUserRegRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Unauthorized API access");
        }

        TenantUserDto dto = tenantPublicApiService.registerEndUser(request, tenantId);

        AppResponse<TenantUserDto> response = AppResponse.<TenantUserDto>builder()
                .message("Successfully registered End User")
                .data(dto)
                .status(HttpStatus.CREATED.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/users/login")
    public ResponseEntity<AppResponse<LoginResponse>> loginEndUser(@RequestBody LoginRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Unauthorized API access");
        }

        LoginResponse loginResponse = tenantPublicApiService.loginEndUser(request, tenantId);

        AppResponse<LoginResponse> response = AppResponse.<LoginResponse>builder()
                .message("Successfully logged in End User")
                .data(loginResponse)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<AppResponse<UserSubscriptionDto>> subscribeUser(
            @RequestBody PublicSubscriptionRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Unauthorized API access");
        }

        UserSubscriptionDto dto = tenantPublicApiService.subscribeUser(request, tenantId);

        AppResponse<UserSubscriptionDto> response = AppResponse.<UserSubscriptionDto>builder()
                .message("Successfully subscribed End User to Tenant Plan")
                .data(dto)
                .status(HttpStatus.CREATED.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

