package com.jobhunt.saas.service;

import com.jobhunt.saas.auth.AuthContext;
import com.jobhunt.saas.dto.DashboardDto;
import com.jobhunt.saas.entity.Subscription;
import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.entity.Tenant;
import com.jobhunt.saas.entity.Users;
import com.jobhunt.saas.repository.SubscriptionRepo;
import com.jobhunt.saas.repository.TenantRepo;
import com.jobhunt.saas.repository.UserRepo;
import com.jobhunt.saas.repository.UserSubscriptionRepo;
import com.jobhunt.saas.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AuthContext authContext;
    private final UserRepo userRepo;
    private final TenantRepo tenantRepo;
    private final SubscriptionRepo subscriptionRepo;
    private final UserSubscriptionRepo userSubscriptionRepo;

    public DashboardDto getDashboard() {

        // 1. Get current logged-in user
        Long userId = authContext.getCurrentUserId();
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Get their tenant
        Tenant tenant = user.getTenant();

        // 3. Get their active SaaS plan subscription (if any)
        Long tenantId = TenantContext.getTenantId();
        Optional<Subscription> activeSubOpt = subscriptionRepo
                .findByUserIdAndTenantIdAndStatus(userId, tenantId, SubscriptionStatus.ACTIVE);

        LocalDateTime planExpiryDate = null;
        long daysRemaining = 0;

        if (activeSubOpt.isPresent()) {
            planExpiryDate = activeSubOpt.get().getEndDate();
            daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), planExpiryDate);
            if (daysRemaining < 0)
                daysRemaining = 0;
        }

        // 4. Count user subscriptions (reference module stats)
        long totalSubs = userSubscriptionRepo.findByUserId(userId).size();
        long activeSubs = userSubscriptionRepo
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE).size();

        // 5. Build and return dashboard response
        return DashboardDto.builder()
                .tenantName(tenant.getName())
                .currentPlan(tenant.getPlan().getName())
                .planPrice(tenant.getPlan().getPrice())
                .status(tenant.getStatus().name())
                .memberSince(tenant.getCreatedAt())
                .planExpiryDate(planExpiryDate)
                .daysRemaining(daysRemaining)
                .clientId(tenant.getClientId())
                .clientSecret(tenant.getClientSecret())
                .apiCallCount(tenant.getApiCallCount())
                .authServiceEnabled(true)
                .subscriptionServiceEnabled(true)
                .emailNotificationsEnabled(true)
                .schedulerEnabled(true)
                .totalUserSubscriptions(totalSubs)
                .activeUserSubscriptions(activeSubs)
                .build();
    }

    // Called from interceptor to increment API call count
    public void incrementApiCallCount(Long tenantId) {
        tenantRepo.findById(tenantId).ifPresent(tenant -> {
            tenant.setApiCallCount(tenant.getApiCallCount() + 1);
            tenantRepo.save(tenant);
        });
    }
}
