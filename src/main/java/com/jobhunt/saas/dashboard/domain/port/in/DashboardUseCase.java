package com.jobhunt.saas.dashboard.domain.port.in;

import com.jobhunt.saas.dashboard.application.dto.DashboardDto;

public interface DashboardUseCase {
    DashboardDto getDashboard();
    void incrementApiCallCount(Long tenantId);
}
