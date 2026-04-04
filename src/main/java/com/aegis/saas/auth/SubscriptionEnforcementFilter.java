package com.aegis.saas.auth;

import com.aegis.saas.entity.SubscriptionStatus;
import com.aegis.saas.repository.TenantSubscriptionRepo;
import com.aegis.saas.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SubscriptionEnforcementFilter extends OncePerRequestFilter {

    private final TenantSubscriptionRepo subscriptionRepo;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Access the Authentication object built by the JWT Filter
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 2. Skip public endpoints to prevent blocking them from signing up
        String path = request.getServletPath();
        if (auth == null
                || path.startsWith("/api/v1/auth")
                || path.startsWith("/api/v1/public")
                || path.equals("/api/v1/users/register")
                || path.equals("/api/v1/users/login")
                || path.equals("/api/v1/users/plans")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Are they the Super Admin? Give them God Mode bypass!
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Enforce the Subscription for regular Tenants
        Long tenantId = TenantContext.getTenantId();

        if (tenantId != null) {
            boolean hasActiveSub = subscriptionRepo.findFirstByTenantIdOrderByCreatedAtDesc(tenantId)
                    .map(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                    .orElse(false);

            if (!hasActiveSub) {
                // NO SUBSCRIPTION? BLOCK THE REQUEST!
                response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED); // Returns a 402 Error
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Payment Required. Your Tenant subscription has expired or is invalid.\"}");
                return; // Notice we DO NOT call filterChain.doFilter! We stop the flow completely.
            }
        }

        // If they passed all checks, let them into the Controller!
        filterChain.doFilter(request, response);
    }
}

