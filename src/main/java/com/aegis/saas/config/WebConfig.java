package com.aegis.saas.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ApiUsageInterceptor apiUsageInterceptor;
    private final ApiKeyInterceptor apiKeyInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(apiUsageInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/v1/auth/**", "/api/v1/health/**", "/error", "/api/v1/users/register", "/api/v1/users/login", "/api/v1/users/plans");

        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        "/api/v1/auth/**",          // Tenant auth — no API key needed
                        "/api/v1/health/**",         // Public health check
                        "/api/v1/super-admin/**",    // Super admin uses JWT
                        "/api/v1/tenant-admin/**",   // Tenant admin uses JWT
                        "/api/v1/users/register",    // End-user register — no key yet
                        "/api/v1/users/login",       // End-user login — no key yet
                        "/api/v1/users/plans"        // Public plan listing
                );
    }
}
