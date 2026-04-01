package com.jobhunt.saas.tenantplan.adapter.out.persistence;

import com.jobhunt.saas.tenantplan.domain.model.TenantPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantPlanRepo extends JpaRepository<TenantPlan, Long> {
    List<TenantPlan> findByTenantId(Long tenantId);

    List<TenantPlan> findByTenantIdAndName(Long tenantId, String name);

    List<TenantPlan> findByTenantIdAndActiveTrue(Long tenantId);
}
