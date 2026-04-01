package com.jobhunt.saas.tenantplan.application.service;

import com.jobhunt.saas.tenantplan.application.dto.TenantPlanDto;
import com.jobhunt.saas.tenant.domain.model.Tenant;
import com.jobhunt.saas.tenantplan.domain.model.TenantPlan;
import com.jobhunt.saas.tenantplan.adapter.out.persistence.TenantPlanRepo;
import com.jobhunt.saas.tenant.adapter.out.persistence.TenantRepo;
import com.jobhunt.saas.shared.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

import com.jobhunt.saas.tenantplan.domain.port.in.TenantPlanUseCase;
@Service
@RequiredArgsConstructor
public class TenantPlanService implements TenantPlanUseCase {

    private final TenantPlanRepo tenantPlanRepo;
    private final TenantRepo tenantRepo;

    public TenantPlan createTenantPlan(TenantPlanDto dto) {
        Long tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        TenantPlan plan = new TenantPlan();
        plan.setTenant(tenant);
        plan.setName(dto.getName());
        plan.setDescription(dto.getDescription());
        plan.setPrice(dto.getPrice());
        plan.setBillingCycle(dto.getBillingCycle());
        plan.setFeatures(dto.getFeatures());
        plan.setActive(true);

        return tenantPlanRepo.save(plan);
    }

    public List<TenantPlan> getPlansForCurrentTenant() {
        Long tenantId = TenantContext.getTenantId();
        return tenantPlanRepo.findByTenantId(tenantId);
    }

    public TenantPlan updatePlan(Long planId, TenantPlanDto dto) {
        TenantPlan plan = tenantPlanRepo.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        if (!plan.getTenant().getId().equals(TenantContext.getTenantId())) {
            throw new RuntimeException("Unauthorized");
        }

        plan.setName(dto.getName());
        plan.setDescription(dto.getDescription());
        plan.setPrice(dto.getPrice());
        plan.setBillingCycle(dto.getBillingCycle());
        plan.setFeatures(dto.getFeatures());

        return tenantPlanRepo.save(plan);
    }

    public void deletePlan(Long planId) {
        TenantPlan plan = tenantPlanRepo.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        if (!plan.getTenant().getId().equals(TenantContext.getTenantId())) {
            throw new RuntimeException("Unauthorized");
        }
        tenantPlanRepo.delete(plan);
    }
}
