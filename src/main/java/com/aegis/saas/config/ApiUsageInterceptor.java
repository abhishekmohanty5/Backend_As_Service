package com.aegis.saas.config;

import com.aegis.saas.service.DashboardService;
import com.aegis.saas.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ApiUsageInterceptor implements HandlerInterceptor {

    private final DashboardService dashboardService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        Long tenantId = TenantContext.getTenantId();

        // Only count authenticated requests (tenantId will be set)
        // Skip auth endpoints and public endpoints
        String uri = request.getRequestURI();
        if (tenantId != null
                && !uri.contains("/api/auth")
                && !uri.contains("/api/public")
                && !uri.contains("/api/dashboard")) {
            dashboardService.incrementApiCallCount(tenantId);
        }

        return true;
    }
}
