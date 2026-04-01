package com.jobhunt.saas.shared.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.jobhunt.saas.tenant.adapter.in.web.ApiUsageInterceptor;
import com.jobhunt.saas.tenant.adapter.in.web.ApiKeyInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ApiUsageInterceptor apiUsageInterceptor;
    private final ApiKeyInterceptor apiKeyInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(apiUsageInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/public", "/error", "/api/v1/**");

        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/api/v1/**");
    }
}
