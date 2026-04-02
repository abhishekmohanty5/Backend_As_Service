package com.aegis.saas.repository;

import com.aegis.saas.entity.TenantPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantPlanRepo extends JpaRepository<TenantPlan, Long> {
    List<TenantPlan> findByTenantId(Long tenantId);

    List<TenantPlan> findByTenantIdAndName(Long tenantId, String name);

    List<TenantPlan> findByTenantIdAndActiveTrue(Long tenantId);
}
