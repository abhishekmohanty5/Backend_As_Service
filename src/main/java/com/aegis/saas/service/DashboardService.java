package com.aegis.saas.service;

import com.aegis.saas.auth.AuthContext;
import com.aegis.saas.dto.DashboardDto;
import com.aegis.saas.entity.SubscriptionStatus;
import com.aegis.saas.entity.Tenant;
import com.aegis.saas.entity.Users;
import com.aegis.saas.exception.ResourceNotFoundException;
import com.aegis.saas.repository.TenantRepo;
import com.aegis.saas.repository.UserRepo;
import com.aegis.saas.repository.UserSubscriptionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AuthContext authContext;
    private final UserRepo userRepo;
    private final TenantRepo tenantRepo;
    private final UserSubscriptionRepo userSubscriptionRepo;

    @Transactional
    public DashboardDto getDashboard() {
        Long userId = authContext.getCurrentUserId();
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Tenant tenant = user.getTenant();

        LocalDateTime planExpiryDate = null;
        long daysRemaining = 0;

        if (tenant != null && tenant.getCreatedAt() != null && tenant.getPlan() != null) {
            planExpiryDate = tenant.getCreatedAt().plusDays(tenant.getPlan().getDurationInDays());
            daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), planExpiryDate);
            if (daysRemaining < 0) daysRemaining = 0;
        }

        long totalSubs = userSubscriptionRepo.findByUserId(userId).size();
        long activeSubs = userSubscriptionRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE).size();

        String tenantName = (tenant != null && tenant.getName() != null) ? tenant.getName() : "My Startup";
        String currentPlanName = (tenant != null && tenant.getPlan() != null) ? tenant.getPlan().getName() : "Free Trial";
        BigDecimal planPrice = (tenant != null && tenant.getPlan() != null) ? tenant.getPlan().getPrice() : BigDecimal.ZERO;
        String status = (tenant != null && tenant.getStatus() != null) ? tenant.getStatus().name() : "INACTIVE";
        LocalDateTime memberSince = (tenant != null && tenant.getCreatedAt() != null) ? tenant.getCreatedAt() : LocalDateTime.now();

        return DashboardDto.builder()
                .tenantName(tenantName)
                .currentPlan(currentPlanName)
                .planPrice(planPrice)
                .status(status)
                .memberSince(memberSince)
                .planExpiryDate(planExpiryDate)
                .daysRemaining(daysRemaining)
                .clientId(tenant != null ? tenant.getClientId() : null)
                .apiCallCount(tenant != null ? tenant.getApiCallCount() : 0L)
                .apiCallLimit(50000L)
                .authServiceEnabled(true)
                .subscriptionServiceEnabled(true)
                .emailNotificationsEnabled(true)
                .schedulerEnabled(true)
                .totalUserSubscriptions(totalSubs)
                .activeUserSubscriptions(activeSubs)
                .build();
    }

    /**
     * Atomic DB-level increment — no read-modify-write race condition.
     */
    @Transactional
    public void incrementApiCallCount(Long tenantId) {
        tenantRepo.incrementApiCallCount(tenantId);
    }
}
