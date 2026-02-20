package com.jobhunt.saas.controller;

import com.jobhunt.saas.dto.AppResponse;
import com.jobhunt.saas.dto.DashboardDto;
import com.jobhunt.saas.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/dashboard")
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
