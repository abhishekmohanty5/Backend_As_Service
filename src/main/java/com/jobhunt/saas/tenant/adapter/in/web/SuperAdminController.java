package com.jobhunt.saas.tenant.adapter.in.web;

import com.jobhunt.saas.shared.dto.AppResponse;
import com.jobhunt.saas.tenant.domain.model.Tenant;
import com.jobhunt.saas.tenant.adapter.out.persistence.TenantRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
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
