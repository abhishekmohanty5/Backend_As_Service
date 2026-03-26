package com.jobhunt.saas.controller;

import com.jobhunt.saas.dto.AppResponse;
import com.jobhunt.saas.dto.EnginePlanUpgradeRequest;
import com.jobhunt.saas.dto.TenantSubscriptionResponseDto;
import com.jobhunt.saas.entity.TenantSubscription;
import com.jobhunt.saas.service.EngineSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tenant-admin/engine-subscription")
@RequiredArgsConstructor
public class EngineSubscriptionController {

    private final EngineSubscriptionService engineSubscriptionService;

    @GetMapping
    public ResponseEntity<AppResponse<TenantSubscriptionResponseDto>> getCurrentSubscription() {
        TenantSubscription sub = engineSubscriptionService.getCurrentSubscription();

        TenantSubscriptionResponseDto dto = mapToDto(sub);

        AppResponse<TenantSubscriptionResponseDto> response = new AppResponse<>(
                "Success", dto, HttpStatus.OK.value(), LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upgrade")
    public ResponseEntity<AppResponse<TenantSubscriptionResponseDto>> upgradeEnginePlan(
            @RequestBody EnginePlanUpgradeRequest request) {

        TenantSubscription upgradedSub = engineSubscriptionService.upgradePlan(
                request.getTargetPlanId(),
                request.getBillingInterval(),
                request.getTransactionId());

        TenantSubscriptionResponseDto dto = mapToDto(upgradedSub);

        AppResponse<TenantSubscriptionResponseDto> response = new AppResponse<>(
                "Successfully upgraded plan", dto, HttpStatus.OK.value(), LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    private TenantSubscriptionResponseDto mapToDto(TenantSubscription sub) {

        return TenantSubscriptionResponseDto.builder()
                .id(sub.getId())
                .tenantName(sub.getTenant().getName())
                .planName(sub.getPlan().getName())
                .amount(sub.getPlan().getPrice())
                .durationInDays(sub.getPlan().getDurationInDays())
                .startDate(sub.getStartDate())
                .expireDate(sub.getExpireDate())
                .status(sub.getStatus().name())
                .build();
    }
}
