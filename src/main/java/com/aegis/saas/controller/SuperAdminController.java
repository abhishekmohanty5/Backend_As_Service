package com.aegis.saas.controller;

import com.aegis.saas.dto.AppResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.aegis.saas.entity.Tenant;
import com.aegis.saas.repository.TenantRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Super Admin - Tenants", description = "Platform-level tenant management for the Aegis platform owner")
@RestController
@RequestMapping("/api/v1/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final TenantRepo tenantRepo;

    @GetMapping("/tenants")
    public ResponseEntity<AppResponse<List<Tenant>>> getAllTenants() {
        List<Tenant> tenants = tenantRepo.findAll();

        AppResponse<List<Tenant>> response = AppResponse.<List<Tenant>>builder()
                .message("Successfully retrieved all Tenants")
                .data(tenants)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
