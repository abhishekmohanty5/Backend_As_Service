package com.aegis.saas.controller;

import com.aegis.saas.dto.AppResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.aegis.saas.dto.EnginePlanUpgradeRequest;
import com.aegis.saas.dto.TenantSubscriptionResponseDto;
import com.aegis.saas.entity.TenantSubscription;
import com.aegis.saas.service.EngineSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Tenant Admin - Billing", description = "Manage the tenant's engine plan subscription (upgrade, view current plan)")
@RestController
@RequestMapping("/api/v1/tenant-admin/billing")
@RequiredArgsConstructor
public class EngineSubscriptionController {

    private final EngineSubscriptionService engineSubscriptionService;

    @GetMapping
    public ResponseEntity<AppResponse<TenantSubscriptionResponseDto>> getCurrentSubscription() {
        TenantSubscriptionResponseDto dto = engineSubscriptionService.getCurrentSubscriptionDto();

        AppResponse<TenantSubscriptionResponseDto> response = new AppResponse<>(
                "Success", dto, HttpStatus.OK.value(), LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upgrade")
    public ResponseEntity<AppResponse<TenantSubscriptionResponseDto>> upgradeEnginePlan(
            @RequestBody EnginePlanUpgradeRequest request) {

        TenantSubscriptionResponseDto dto = engineSubscriptionService.upgradePlanDto(
                request.getTargetPlanId(),
                request.getBillingInterval(),
                request.getTransactionId());

        AppResponse<TenantSubscriptionResponseDto> response = new AppResponse<>(
                "Successfully upgraded plan", dto, HttpStatus.OK.value(), LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
