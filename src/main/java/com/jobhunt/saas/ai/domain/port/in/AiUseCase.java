package com.jobhunt.saas.ai.domain.port.in;

import com.jobhunt.saas.tenantplan.application.dto.TenantPlanDto;

import java.util.List;

public interface AiUseCase {
    String generateSubscriptionAnalytics(Long tenantId);
    List<TenantPlanDto> generatePricingPlans(String businessDescription);
    String predictChurnRisk(Long userId, Long tenantId);
}
