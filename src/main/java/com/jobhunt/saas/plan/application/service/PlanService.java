package com.jobhunt.saas.plan.application.service;

import com.jobhunt.saas.plan.application.dto.PlanRequest;
import com.jobhunt.saas.plan.domain.model.Plan;
import com.jobhunt.saas.plan.adapter.out.persistence.PlanRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import com.jobhunt.saas.plan.domain.port.in.PlanUseCase;
@Service
@RequiredArgsConstructor
public class PlanService implements PlanUseCase {

     private final PlanRepo planRepo;

     @CacheEvict(cacheNames = "Plans", allEntries = true)
    public void createPlan(PlanRequest planRequest) {

        Plan plan = new Plan();
        plan.setName(planRequest.getName());
        plan.setPrice(planRequest.getPrice());
        plan.setDurationInDays(planRequest.getDurationInDays());
        plan.setActive(true);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        planRepo.save(plan);
    }

    @Cacheable(cacheNames = "Plans")
    public List<Plan> findAll() {
        return planRepo.findAll();
    }


    @CacheEvict(cacheNames = "Plans", allEntries = true)
    public void activatePlan(Long id) {
        Plan plan = planRepo.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException("Plan with ID " + id + " not found"));

        plan.setActive(true);
        plan.setUpdatedAt(LocalDateTime.now());
        planRepo.save(plan);
    }

    @CacheEvict(cacheNames = "Plans", allEntries = true)
    public void deactivatePlan(Long id) {
        Plan plan=planRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Plan with ID " + id + " not found"));

        plan.setActive(false);
        plan.setUpdatedAt(LocalDateTime.now());
        planRepo.save(plan);
    }

}
