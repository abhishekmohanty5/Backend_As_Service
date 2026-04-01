package com.jobhunt.saas.tenant.domain.port.in;

import com.jobhunt.saas.tenant.domain.model.Tenant;

import java.util.List;

public interface GetTenantsUseCase {
    List<Tenant> getAllTenants();
}
