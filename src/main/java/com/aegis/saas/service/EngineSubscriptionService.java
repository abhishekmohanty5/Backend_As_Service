package com.aegis.saas.service;

import com.aegis.saas.dto.TenantSubscriptionResponseDto;
import com.aegis.saas.entity.Plan;
import com.aegis.saas.entity.SubscriptionStatus;
import com.aegis.saas.entity.Tenant;
import com.aegis.saas.entity.TenantSubscription;
import com.aegis.saas.exception.BusinessException;
import com.aegis.saas.exception.ResourceNotFoundException;
import com.aegis.saas.repository.PlanRepo;
import com.aegis.saas.repository.TenantRepo;
import com.aegis.saas.repository.TenantSubscriptionRepo;
import com.aegis.saas.tenant.TenantContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EngineSubscriptionService {

    private final TenantRepo tenantRepo;
    private final PlanRepo planRepo;
    private final TenantSubscriptionRepo tenantSubscriptionRepo;

    @Transactional
    public TenantSubscription getCurrentSubscription() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context not resolved. Please ensure your token is valid.");
        }
        return tenantSubscriptionRepo.findFirstByTenantIdOrderByCreatedAtDesc(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found for this tenant."));
    }

    @Transactional
    public TenantSubscriptionResponseDto upgradePlanDto(Long newPlanId, String billingInterval, String transactionId) {
        TenantSubscription upgradedSub = upgradePlan(newPlanId, billingInterval, transactionId);
        return mapToDto(upgradedSub);
    }

    @Transactional
    public TenantSubscription upgradePlan(Long newPlanId, String billingInterval, String transactionId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context not resolved. Please ensure your token is valid.");
        }

        if (transactionId == null || transactionId.isBlank()) {
            throw new BusinessException("Payment transaction ID is required. Please complete the payment step first.");
        }

        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        Plan targetPlan = planRepo.findById(newPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", newPlanId));

        if (!targetPlan.isActive()) {
            throw new BusinessException("The plan '" + targetPlan.getName() + "' is no longer available.");
        }

        int durationDays = "ANNUAL".equalsIgnoreCase(billingInterval) ? 365 : 30;

        tenantSubscriptionRepo.findFirstByTenantIdOrderByCreatedAtDesc(tenantId)
                .ifPresent(currentSub -> {
                    currentSub.setStatus(SubscriptionStatus.CANCELLED);
                    currentSub.setExpireDate(LocalDateTime.now());
                    tenantSubscriptionRepo.save(currentSub);
                });

        TenantSubscription newSub = new TenantSubscription();
        newSub.setTenant(tenant);
        newSub.setPlan(targetPlan);
        newSub.setStatus(SubscriptionStatus.ACTIVE);
        newSub.setStartDate(LocalDateTime.now());
        newSub.setExpireDate(LocalDateTime.now().plusDays(durationDays));
        return tenantSubscriptionRepo.save(newSub);
    }

    @Transactional
    public TenantSubscriptionResponseDto getCurrentSubscriptionDto() {
        TenantSubscription sub = getCurrentSubscription();
        return mapToDto(sub);
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
