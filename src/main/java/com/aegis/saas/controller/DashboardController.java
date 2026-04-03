package com.aegis.saas.controller;

import com.aegis.saas.dto.AppResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.aegis.saas.dto.DashboardDto;
import com.aegis.saas.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "Tenant Admin - Dashboard", description = "Developer console overview: tenant info, plan expiry, API credentials and usage stats")
@RestController
@RequestMapping("/api/v1/tenant-admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard
     * Returns full developer console data:
     * - Tenant info (name, plan, status, memberSince)
     * - Plan expiry & days remaining
     * - API credentials (clientId, clientSecret)
     * - API usage counter
     * - Enabled services
     * - Reference module stats (total & active user subscriptions)
     */
    @GetMapping
    public ResponseEntity<AppResponse<DashboardDto>> getDashboard() {

        DashboardDto data = dashboardService.getDashboard();

        AppResponse<DashboardDto> response = AppResponse.<DashboardDto>builder()
                .message("Success")
                .data(data)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
