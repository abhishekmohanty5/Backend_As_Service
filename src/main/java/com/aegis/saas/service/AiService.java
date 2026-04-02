package com.aegis.saas.service;

import com.aegis.saas.dto.TenantPlanDto;

import java.util.List;

public interface AiService {

    /**
     * Generates a conversational analysis of a tenant's subscription performance.
     * 
     * @param tenantId The ID of the tenant.
     * @return AI-generated text analyzing revenue, active subscriptions, and
     *         potential churn.
     */
    String generateSubscriptionAnalytics(Long tenantId);

    /**
     * Suggests 3 pricing plans based on a description of the tenant's business.
     * 
     * @param businessDescription A brief description of what the business does.
     * @return A list of 3 suggested TenantPlanDto objects.
     */
    List<TenantPlanDto> generatePricingPlans(String businessDescription);

    /**
     * Predicts the likelihood of an end-user churning based on their subscription
     * history.
     * 
     * @param userId   The ID of the user.
     * @param tenantId The ID of the tenant to ensure data isolation.
     * @return AI-generated string assessing the churn risk.
     */
    String predictChurnRisk(Long userId, Long tenantId);
}
