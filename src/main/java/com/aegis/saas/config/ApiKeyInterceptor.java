package com.aegis.saas.config;

import com.aegis.saas.entity.SubscriptionStatus;
import com.aegis.saas.entity.Tenant;
import com.aegis.saas.repository.TenantRepo;
import com.aegis.saas.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyInterceptor implements HandlerInterceptor {

    private final TenantRepo tenantRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String uri = request.getRequestURI();

        // 1. Only intercept external /api/v1 routes
        if (!uri.startsWith("/api/v1/")) {
            return true;
        }

        // 2. Extract API Keys
        String clientId = request.getHeader("X-API-CLIENT-ID");
        String clientSecret = request.getHeader("X-API-CLIENT-SECRET");

        // 🔑 Allow bypass if already authenticated via JWT (Tenant Admin in Dashboard)
        if (TenantContext.getTenantId() != null) {
            return true;
        }

        if (clientId == null || clientSecret == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(),
                    "Missing API Credentials. Please provide X-API-CLIENT-ID and X-API-CLIENT-SECRET headers.");
            return false;
        }

        // 3. Validate Tenant
        Optional<Tenant> tenantOpt = tenantRepo.findByClientId(clientId);
        if (tenantOpt.isEmpty() || tenantOpt.get().getStatus() != SubscriptionStatus.ACTIVE) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Invalid or Inactive API Credentials.");
            return false;
        }

        Tenant tenant = tenantOpt.get();

        String storedSecret = tenant.getClientSecret();
        boolean secretMatches = false;

        if (storedSecret != null) {
            if (isBcryptHash(storedSecret)) {
                secretMatches = passwordEncoder.matches(clientSecret, storedSecret);
            } else {
                secretMatches = storedSecret.equals(clientSecret);

                // Lazily migrate legacy plain-text secrets to bcrypt once they are validated.
                if (secretMatches) {
                    tenant.setClientSecret(passwordEncoder.encode(clientSecret));
                    tenantRepo.save(tenant);
                }
            }
        }

        if (!secretMatches) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Invalid or Inactive API Credentials.");
            return false;
        }

        // 4. Plan & Route Blocking Validation
        String planName = tenant.getPlan().getName().toUpperCase();

        // Example Route Blocking Rule:
        // If route requires advanced features, only Enterprise plan allowed
        if (uri.contains("/ai-agent") && !"ENTERPRISE".equals(planName)) {
            response.sendError(HttpStatus.PAYMENT_REQUIRED.value(), "Upgrade to Enterprise to access AI Features.");
            return false;
        }

        long limit = getApiLimitForPlan(planName);
        long currentCalls = tenant.getApiCallCount() != null ? tenant.getApiCallCount() : 0;

        if (currentCalls >= limit) {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "API Rate Limit Exceeded for plan " + planName);
            return false;
        }

        // 5. Setup Context (so downstream controllers know which Tenant is acting)
        TenantContext.setTenantId(tenant.getId());

        // 6. Increment API Call Count
        tenant.setApiCallCount(tenant.getApiCallCount() + 1);
        tenantRepo.save(tenant);

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler,
                                Exception ex) {
        // Clear the tenant context after the request completes to prevent memory leaks
        // in the thread pool
        if (request.getRequestURI().startsWith("/api/v1/")) {
            TenantContext.clear();
        }
    }

    private long getApiLimitForPlan(String planName) {
        if ("FREE".equalsIgnoreCase(planName))
            return 100;
        if ("STARTER".equalsIgnoreCase(planName))
            return 5000;
        if ("PRO".equalsIgnoreCase(planName))
            return 20000;
        return 100000; // Premium or others
    }

    private boolean isBcryptHash(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
