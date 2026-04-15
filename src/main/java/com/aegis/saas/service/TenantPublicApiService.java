package com.aegis.saas.service;

import com.aegis.saas.dto.*;
import com.aegis.saas.entity.Role;
import com.aegis.saas.entity.SubscriptionStatus;
import com.aegis.saas.entity.Tenant;
import com.aegis.saas.entity.TenantPlan;
import com.aegis.saas.entity.Users;
import com.aegis.saas.entity.UserSubscription;
import com.aegis.saas.repository.TenantPlanRepo;
import com.aegis.saas.repository.TenantRepo;
import com.aegis.saas.repository.UserRepo;
import com.aegis.saas.repository.UserSubscriptionRepo;
import com.aegis.saas.auth.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantPublicApiService {

    private final UserRepo userRepo;
    private final TenantRepo tenantRepo;
    private final TenantPlanRepo tenantPlanRepo;
    private final UserSubscriptionRepo userSubscriptionRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    public List<TenantPlanResponseDto> getPublicTenantPlans(Long tenantId) {
        List<TenantPlan> plans = tenantPlanRepo.findByTenantIdAndActiveTrue(tenantId);
        return plans.stream().map(p -> TenantPlanResponseDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .billingCycle(p.getBillingCycle())
                .features(p.getFeatures())
                .active(p.isActive())
                .build()).toList();
    }

    public TenantUserDto registerEndUser(EndUserRegRequest request, Long tenantId) {
        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not resolved from API Key"));

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Users user = new Users();
        user.setUsername(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setTenant(tenant);

        userRepo.save(user);

        return TenantUserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public LoginResponse loginEndUser(LoginRequest request, Long tenantId) {
        Users user = userRepo.findByEmailAndTenant_Id(request.getEmail(), tenantId);
        if (user == null) {
            throw new RuntimeException("Invalid credentials or user not found in this tenant.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials.");
        }

        String token = jwtService.generateToken(user.getEmail(), tenantId);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setEmail(user.getEmail());
        loginResponse.setName(user.getUsername());
        loginResponse.setRole(user.getRole());
        return loginResponse;
    }

    public UserSubscriptionDto subscribeUser(PublicSubscriptionRequest request, Long tenantId) {
        Users user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("User belongs to a different Tenant");
        }

        TenantPlan plan = tenantPlanRepo.findById(request.getTenantPlanId())
                .orElseThrow(() -> new RuntimeException("Tenant Plan not found"));

        if (!plan.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Tenant Plan belongs to a different Tenant");
        }

        LocalDate startDate = LocalDate.now();
        LocalDate nextBillingDate = startDate;

        if (plan.getBillingCycle() != null) {
            switch (plan.getBillingCycle()) {
                case MONTHLY:
                    nextBillingDate = startDate.plusMonths(1);
                    break;
                case YEARLY:
                    nextBillingDate = startDate.plusYears(1);
                    break;
                case WEEKLY:
                    nextBillingDate = startDate.plusWeeks(1);
                    break;
            }
        } else {
            nextBillingDate = startDate.plusMonths(1);
        }

        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .subscriptionName(plan.getName())
                .tenantPlan(plan)
                .amount(plan.getPrice())
                .billingCycle(plan.getBillingCycle())
                .startDate(startDate)
                .nextBillingDate(nextBillingDate)
                .status(SubscriptionStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        userSubscriptionRepo.save(subscription);

        return UserSubscriptionDto.builder()
                .id(subscription.getId())
                .userId(subscription.getUser().getId())
                .tenantPlanId(subscription.getTenantPlan().getId())
                .username(subscription.getUser().getUsername())
                .subscriptionName(subscription.getSubscriptionName())
                .amount(subscription.getAmount())
                .billingCycle(subscription.getBillingCycle())
                .startDate(subscription.getStartDate())
                .nextBillingDate(subscription.getNextBillingDate())
                .status(subscription.getStatus())
                .notes(subscription.getNotes())
                .build();
    }
}
