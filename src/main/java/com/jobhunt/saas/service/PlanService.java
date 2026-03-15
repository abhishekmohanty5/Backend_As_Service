package com.jobhunt.saas.service;

import com.jobhunt.saas.dto.PlanRequest;
import com.jobhunt.saas.entity.Plan;
import com.jobhunt.saas.repository.PlanRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

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
