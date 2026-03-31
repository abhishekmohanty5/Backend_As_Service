package com.jobhunt.saas.controller;

import com.jobhunt.saas.dto.*;
import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.entity.Tenant;
import com.jobhunt.saas.entity.TenantPlan;
import com.jobhunt.saas.entity.Users;
import com.jobhunt.saas.entity.UserSubscription;
import com.jobhunt.saas.repository.TenantRepo;
import com.jobhunt.saas.repository.UserRepo;
import com.jobhunt.saas.repository.UserSubscriptionRepo;
import com.jobhunt.saas.service.TenantPlanService;
import com.jobhunt.saas.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/developer")
@RequiredArgsConstructor
public class TenantDeveloperController {

        private final TenantPlanService tenantPlanService;
        private final TenantRepo tenantRepo;
        private final UserRepo userRepo;
        private final UserSubscriptionRepo userSubscriptionRepo;

        // --- API KEY MANAGEMENT ---

        @GetMapping("/keys")
        public ResponseEntity<AppResponse<Map<String, String>>> getApiKeys() {
                Long tenantId = TenantContext.getTenantId();
                Tenant tenant = tenantRepo.findById(tenantId)
                                .orElseThrow(() -> new RuntimeException("Tenant not found"));

                Map<String, String> keys = new HashMap<>();
                keys.put("clientId", tenant.getClientId());
                keys.put("clientSecret", tenant.getClientSecret());

                AppResponse<Map<String, String>> response = AppResponse.<Map<String, String>>builder()
                                .message("Successfully retrieved API keys")
                                .data(keys)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        // --- TENANT PLAN MANAGEMENT ---

        @PostMapping("/tenant-plans")
        public ResponseEntity<AppResponse<TenantPlanResponseDto>> createTenantPlan(@RequestBody TenantPlanDto request) {
                TenantPlan plan = tenantPlanService.createTenantPlan(request);
                TenantPlanResponseDto dto = mapToTenantPlanResponseDto(plan);

                AppResponse<TenantPlanResponseDto> response = AppResponse.<TenantPlanResponseDto>builder()
                                .message("Successfully created Tenant Plan")
                                .data(dto)
                                .status(HttpStatus.CREATED.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @GetMapping("/tenant-plans")
        public ResponseEntity<AppResponse<List<TenantPlanResponseDto>>> getTenantPlans() {
                List<TenantPlan> plans = tenantPlanService.getPlansForCurrentTenant();
                List<TenantPlanResponseDto> dtos = plans.stream()
                                .map(this::mapToTenantPlanResponseDto)
                                .toList();

                AppResponse<List<TenantPlanResponseDto>> response = AppResponse.<List<TenantPlanResponseDto>>builder()
                                .message("Successfully retrieved Tenant Plans")
                                .data(dtos)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        @PutMapping("/tenant-plans/{id}")
        public ResponseEntity<AppResponse<TenantPlanResponseDto>> updateTenantPlan(@PathVariable Long id,
                        @RequestBody TenantPlanDto request) {
                TenantPlan plan = tenantPlanService.updatePlan(id, request);
                TenantPlanResponseDto dto = mapToTenantPlanResponseDto(plan);

                AppResponse<TenantPlanResponseDto> response = AppResponse.<TenantPlanResponseDto>builder()
                                .message("Successfully updated Tenant Plan")
                                .data(dto)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/tenant-plans/{id}")
        public ResponseEntity<AppResponse<Void>> deleteTenantPlan(@PathVariable Long id) {
                tenantPlanService.deletePlan(id);

                AppResponse<Void> response = AppResponse.<Void>builder()
                                .message("Successfully deleted Tenant Plan")
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/users")
        public ResponseEntity<AppResponse<List<TenantUserDto>>> getTenantUsers() {
                Long tenantId = TenantContext.getTenantId();
                List<Users> users = userRepo.findByTenantId(tenantId);
                List<TenantUserDto> dtos = users.stream()
                                .map(u -> TenantUserDto.builder()
                                                .id(u.getId())
                                                .username(u.getUsername())
                                                .email(u.getEmail())
                                                .role(u.getRole())
                                                .build())
                                .toList();

                AppResponse<List<TenantUserDto>> response = AppResponse.<List<TenantUserDto>>builder()
                                .message("Successfully retrieved End Users")
                                .data(dtos)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/user-subscriptions")
        public ResponseEntity<AppResponse<List<UserSubscriptionDto>>> getTenantUserSubscriptions() {
                Long tenantId = TenantContext.getTenantId();
                List<UserSubscription> subscriptions = userSubscriptionRepo.findByUser_TenantId(tenantId);
                List<UserSubscriptionDto> dtos = subscriptions.stream()
                                .map(s -> UserSubscriptionDto.builder()
                                                .id(s.getId())
                                                .userId(s.getUser().getId())
                                                .tenantPlanId(s.getTenantPlan().getId())
                                                .username(s.getUser().getUsername())
                                                .subscriptionName(s.getSubscriptionName())
                                                .amount(s.getAmount())
                                                .billingCycle(s.getBillingCycle())
                                                .startDate(s.getStartDate())
                                                .nextBillingDate(s.getNextBillingDate())
                                                .status(s.getStatus())
                                                .notes(s.getNotes())
                                                .build())
                                .toList();

                AppResponse<List<UserSubscriptionDto>> response = AppResponse.<List<UserSubscriptionDto>>builder()
                                .message("Successfully retrieved End User Subscriptions")
                                .data(dtos)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        // --- SUBSCRIBER STATS ---

        @GetMapping("/tenant-stats")
        public ResponseEntity<AppResponse<Map<String, Long>>> getTenantStats() {
                Long tenantId = TenantContext.getTenantId();
                List<UserSubscription> all = userSubscriptionRepo.findByUser_TenantId(tenantId);

                long total = all.size();
                long active = all.stream().filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE).count();
                long cancelled = all.stream().filter(s -> s.getStatus() == SubscriptionStatus.CANCELLED).count();
                long expired = all.stream().filter(s -> s.getStatus() == SubscriptionStatus.EXPIRED).count();

                Map<String, Long> statsMap = new HashMap<>();
                statsMap.put("total", total);
                statsMap.put("active", active);
                statsMap.put("cancelled", cancelled);
                statsMap.put("pending", expired);

                AppResponse<Map<String, Long>> response = AppResponse.<Map<String, Long>>builder()
                                .message("Successfully retrieved Tenant Stats")
                                .data(statsMap)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/tenant-subscribers")
        public ResponseEntity<AppResponse<List<UserSubscriptionDto>>> getTenantSubscribers() {
                Long tenantId = TenantContext.getTenantId();
                List<UserSubscription> subscriptions = userSubscriptionRepo.findByUser_TenantId(tenantId);
                List<UserSubscriptionDto> dtos = subscriptions.stream()
                                .map(s -> UserSubscriptionDto.builder()
                                                .id(s.getId())
                                                .userId(s.getUser().getId())
                                                .tenantPlanId(s.getTenantPlan().getId())
                                                .username(s.getUser().getUsername())
                                                .subscriptionName(s.getSubscriptionName())
                                                .amount(s.getAmount())
                                                .billingCycle(s.getBillingCycle())
                                                .startDate(s.getStartDate())
                                                .nextBillingDate(s.getNextBillingDate())
                                                .status(s.getStatus())
                                                .notes(s.getNotes())
                                                .build())
                                .toList();

                AppResponse<List<UserSubscriptionDto>> response = AppResponse.<List<UserSubscriptionDto>>builder()
                                .message("Successfully retrieved Tenant Subscribers")
                                .data(dtos)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        private TenantPlanResponseDto mapToTenantPlanResponseDto(TenantPlan plan) {
                return TenantPlanResponseDto.builder()
                                .id(plan.getId())
                                .name(plan.getName())
                                .description(plan.getDescription())
                                .price(plan.getPrice())
                                .billingCycle(plan.getBillingCycle())
                                .features(plan.getFeatures())
                                .active(plan.isActive())
                                .build();
        }
}
