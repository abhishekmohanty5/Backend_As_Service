package com.jobhunt.saas.controller;

import com.jobhunt.saas.dto.*;
import com.jobhunt.saas.entity.Role;
import com.jobhunt.saas.entity.Tenant;
import com.jobhunt.saas.entity.TenantPlan;
import com.jobhunt.saas.entity.Users;
import com.jobhunt.saas.entity.UserSubscription;
import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.repository.TenantPlanRepo;
import com.jobhunt.saas.repository.TenantRepo;
import com.jobhunt.saas.repository.UserRepo;
import com.jobhunt.saas.repository.UserSubscriptionRepo;
import com.jobhunt.saas.service.AiService;
import com.jobhunt.saas.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TenantPublicApiController {

    private final UserRepo userRepo;
    private final TenantRepo tenantRepo;
    private final TenantPlanRepo tenantPlanRepo;
    private final UserSubscriptionRepo userSubscriptionRepo;
    private final PasswordEncoder passwordEncoder;
    private final AiService aiService;
    private final com.jobhunt.saas.auth.JWTService jwtService;

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

        // Verify the user belongs to the tenant
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("User belongs to a different Tenant");
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

        // Only return ACTIVE plans to the public, preventing access to sensitive/draft
        // plans
        java.util.List<TenantPlan> plans = tenantPlanRepo.findByTenantIdAndActiveTrue(tenantId);

        java.util.List<TenantPlanResponseDto> dtos = plans.stream().map(p -> TenantPlanResponseDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .billingCycle(p.getBillingCycle())
                .features(p.getFeatures())
                .active(p.isActive())
                .build()).toList();

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

        TenantUserDto dto = TenantUserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

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
        loginResponse.setRole(user.getRole());

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

        // Simple billing cycle logic for demo
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

        UserSubscriptionDto dto = UserSubscriptionDto.builder()
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

        AppResponse<UserSubscriptionDto> response = AppResponse.<UserSubscriptionDto>builder()
                .message("Successfully subscribed End User to Tenant Plan")
                .data(dto)
                .status(HttpStatus.CREATED.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
