package com.aegis.saas.service;

import com.aegis.saas.dto.TenantPlanDto;
import com.aegis.saas.entity.Tenant;
import com.aegis.saas.entity.TenantPlan;
import com.aegis.saas.exception.ResourceNotFoundException;
import com.aegis.saas.exception.UnauthorizedException;
import com.aegis.saas.repository.TenantPlanRepo;
import com.aegis.saas.repository.TenantRepo;
import com.aegis.saas.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantPlanService {

    private final TenantPlanRepo tenantPlanRepo;
    private final TenantRepo tenantRepo;

    public TenantPlan createTenantPlan(TenantPlanDto dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new com.aegis.saas.exception.BusinessException("Tenant context not resolved.");
        }
        final long resolvedId = tenantId;
        Tenant tenant = tenantRepo.findById(resolvedId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", resolvedId));

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
        return tenantPlanRepo.findByTenantId(TenantContext.getTenantId());
    }

    public TenantPlan updatePlan(Long planId, TenantPlanDto dto) {
        TenantPlan plan = tenantPlanRepo.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("TenantPlan", planId));
        if (!plan.getTenant().getId().equals(TenantContext.getTenantId())) {
            throw new UnauthorizedException("You do not have permission to modify this plan.");
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
                .orElseThrow(() -> new ResourceNotFoundException("TenantPlan", planId));
        if (!plan.getTenant().getId().equals(TenantContext.getTenantId())) {
            throw new UnauthorizedException("You do not have permission to delete this plan.");
        }
        tenantPlanRepo.delete(plan);
    }
}
