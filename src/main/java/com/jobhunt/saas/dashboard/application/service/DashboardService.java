package com.jobhunt.saas.dashboard.application.service;

import com.jobhunt.saas.auth.application.service.AuthContext;
import com.jobhunt.saas.dashboard.application.dto.DashboardDto;
import com.jobhunt.saas.shared.domain.model.SubscriptionStatus;
import com.jobhunt.saas.tenant.domain.model.Tenant;
import com.jobhunt.saas.user.domain.model.Users;
import com.jobhunt.saas.tenant.adapter.out.persistence.TenantRepo;
import com.jobhunt.saas.user.adapter.out.persistence.UserRepo;
import com.jobhunt.saas.subscription.adapter.out.persistence.UserSubscriptionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.jobhunt.saas.dashboard.domain.port.in.DashboardUseCase;
@Service
@RequiredArgsConstructor
public class DashboardService implements DashboardUseCase {

        private final AuthContext authContext;
        private final UserRepo userRepo;
        private final TenantRepo tenantRepo;
        private final UserSubscriptionRepo userSubscriptionRepo;

        @jakarta.transaction.Transactional
        public DashboardDto getDashboard() {

                // 1. Get current logged-in user
                Long userId = authContext.getCurrentUserId();
                Users user = userRepo.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // 2. Get their tenant
                Tenant tenant = user.getTenant();

                // 3. Get their active SaaS plan subscription (if any)
                LocalDateTime planExpiryDate = null;
                long daysRemaining = 0;

                if (tenant != null && tenant.getCreatedAt() != null && tenant.getPlan() != null) {
                        planExpiryDate = tenant.getCreatedAt().plusDays(tenant.getPlan().getDurationInDays());
                        daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), planExpiryDate);
                        if (daysRemaining < 0)
                                daysRemaining = 0;
                }

                // 4. Count user subscriptions (reference module stats)
                long totalSubs = userSubscriptionRepo.findByUserId(userId).size();
                long activeSubs = userSubscriptionRepo
                                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE).size();

                // 5. Build and return dashboard response
                String tenantName = (tenant != null && tenant.getName() != null) ? tenant.getName() : "My Startup";
                String currentPlanNm = (tenant != null && tenant.getPlan() != null) ? tenant.getPlan().getName()
                                : "Free Trial";
                java.math.BigDecimal planPrice = (tenant != null && tenant.getPlan() != null)
                                ? tenant.getPlan().getPrice()
                                : java.math.BigDecimal.ZERO;
                String status = (tenant != null && tenant.getStatus() != null) ? tenant.getStatus().name() : "INACTIVE";
                LocalDateTime memberSince = (tenant != null && tenant.getCreatedAt() != null) ? tenant.getCreatedAt()
                                : LocalDateTime.now();

                return DashboardDto.builder()
                                .tenantName(tenantName)
                                .currentPlan(currentPlanNm)
                                .planPrice(planPrice)
                                .status(status)
                                .memberSince(memberSince)
                                .planExpiryDate(planExpiryDate)
                                .daysRemaining(daysRemaining)
                                .clientId(tenant != null ? tenant.getClientId() : null)
                                .clientSecret(tenant != null ? tenant.getClientSecret() : null)
                                .apiCallCount(tenant != null ? tenant.getApiCallCount() : 0L)
                                .apiCallLimit(50000L) // Default limit
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
