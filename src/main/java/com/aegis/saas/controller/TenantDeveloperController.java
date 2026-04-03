package com.aegis.saas.controller;

import com.aegis.saas.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.aegis.saas.entity.Tenant;
import com.aegis.saas.entity.TenantPlan;
import com.aegis.saas.entity.Users;
import com.aegis.saas.repository.TenantRepo;
import com.aegis.saas.repository.UserRepo;
import com.aegis.saas.service.TenantPlanService;
import com.aegis.saas.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Tag(name = "Tenant Admin - Settings", description = "Manage API keys, tenant plans and view end users")
@RestController
@RequestMapping("/api/v1/tenant-admin")
@RequiredArgsConstructor
public class TenantDeveloperController {

        private final TenantPlanService tenantPlanService;
        private final TenantRepo tenantRepo;
        private final UserRepo userRepo;

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

        // --- USER MANAGEMENT ---

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
