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
import com.aegis.saas.auth.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Tag(name = "Tenant Admin - Settings", description = "Manage API keys, tenant plans and view end users")
@RestController
@RequestMapping("/api/v1/tenant-admin")
@RequiredArgsConstructor
@Slf4j
@org.springframework.transaction.annotation.Transactional
public class TenantDeveloperController {

        private final TenantPlanService tenantPlanService;
        private final TenantRepo tenantRepo;
        private final UserRepo userRepo;
        private final AuthContext authContext;
        private final PasswordEncoder passwordEncoder;

        // --- API KEY MANAGEMENT ---

        @GetMapping("/keys")
        public ResponseEntity<AppResponse<Map<String, Object>>> getApiKeys() {
                Long userId = authContext.getCurrentUserId();
                Users user = userRepo.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                Tenant tenant = user.getTenant();

                if (tenant == null) {
                        throw new RuntimeException("No tenant associated with user");
                }

                log.info("Retrieving API keys for tenant: {} (User: {})", tenant.getId(), userId);

                Map<String, Object> keys = new HashMap<>();
                keys.put("clientId", tenant.getClientId());
                keys.put("hasSecret", tenant.getClientSecret() != null);
                keys.put("secretPreview", tenant.getClientSecret() != null ? "sk_************" : null);
                keys.put("secretGeneratedAt", tenant.getClientSecretGeneratedAt());

                AppResponse<Map<String, Object>> response = AppResponse.<Map<String, Object>>builder()
                                .message("Successfully retrieved API keys")
                                .data(keys)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        @PostMapping("/keys/regenerate")
        public ResponseEntity<AppResponse<Map<String, Object>>> regenerateApiKeys() {
                Long userId = authContext.getCurrentUserId();
                Users user = userRepo.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                Tenant tenant = user.getTenant();

                if (tenant == null) {
                        throw new RuntimeException("No tenant associated with user");
                }

                log.info("Regenerating API keys for tenant: {} (User: {})", tenant.getId(), userId);

                // Generate new client secret
                String newSecret = "sk_" + UUID.randomUUID().toString().replace("-", "");
                tenant.setClientSecret(passwordEncoder.encode(newSecret));
                tenant.setClientSecretGeneratedAt(LocalDateTime.now());
                tenantRepo.save(tenant);

                log.info("Successfully generated new secret for tenant: {}", tenant.getId());

                Map<String, Object> keys = new HashMap<>();
                keys.put("clientId", tenant.getClientId());
                keys.put("clientSecret", newSecret);
                keys.put("hasSecret", true);
                keys.put("secretPreview", "sk_************");
                keys.put("secretGeneratedAt", LocalDateTime.now());

                AppResponse<Map<String, Object>> response = AppResponse.<Map<String, Object>>builder()
                                .message("Successfully regenerated API client secret. Old secret is now invalid.")
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
        public ResponseEntity<AppResponse<PaginatedResponse<TenantPlanResponseDto>>> getTenantPlans(Pageable pageable) {
                Page<TenantPlan> plans = tenantPlanService.getPlansForCurrentTenant(pageable);
                Page<TenantPlanResponseDto> mapped = plans.map(this::mapToTenantPlanResponseDto);

                AppResponse<PaginatedResponse<TenantPlanResponseDto>> response = AppResponse.<PaginatedResponse<TenantPlanResponseDto>>builder()
                                .message("Successfully retrieved Tenant Plans")
                                .data(PaginatedResponse.from(mapped))
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        @PutMapping("/tenant-plans/{id}/activate")
        public ResponseEntity<AppResponse<TenantPlanResponseDto>> activateTenantPlan(@PathVariable Long id) {
                TenantPlan plan = tenantPlanService.activatePlan(id);
                TenantPlanResponseDto dto = mapToTenantPlanResponseDto(plan);

                AppResponse<TenantPlanResponseDto> response = AppResponse.<TenantPlanResponseDto>builder()
                                .message("Successfully activated Tenant Plan")
                                .data(dto)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        @PutMapping("/tenant-plans/{id}/deactivate")
        public ResponseEntity<AppResponse<TenantPlanResponseDto>> deactivateTenantPlan(@PathVariable Long id) {
                TenantPlan plan = tenantPlanService.deactivatePlan(id);
                TenantPlanResponseDto dto = mapToTenantPlanResponseDto(plan);

                AppResponse<TenantPlanResponseDto> response = AppResponse.<TenantPlanResponseDto>builder()
                                .message("Successfully deactivated Tenant Plan")
                                .data(dto)
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
