package com.jobhunt.saas.tenantplan.domain.port.in;

import com.jobhunt.saas.tenantplan.application.dto.TenantPlanDto;
import com.jobhunt.saas.tenantplan.domain.model.TenantPlan;

import java.util.List;

public interface TenantPlanUseCase {
    TenantPlan createTenantPlan(TenantPlanDto dto);
    List<TenantPlan> getPlansForCurrentTenant();
    TenantPlan updatePlan(Long planId, TenantPlanDto dto);
    void deletePlan(Long planId);
}
