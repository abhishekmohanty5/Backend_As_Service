package com.jobhunt.saas.plan.domain.port.in;

import com.jobhunt.saas.plan.application.dto.PlanRequest;
import com.jobhunt.saas.plan.domain.model.Plan;

import java.util.List;

public interface PlanUseCase {
    void createPlan(PlanRequest planRequest);
    List<Plan> findAll();
    void activatePlan(Long id);
    void deactivatePlan(Long id);
}
