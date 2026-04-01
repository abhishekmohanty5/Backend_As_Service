package com.jobhunt.saas.plan.adapter.in.web;

import com.jobhunt.saas.shared.dto.AppResponse;
import com.jobhunt.saas.plan.domain.model.Plan;
import com.jobhunt.saas.plan.application.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<AppResponse<List<Plan>>> getPlans() {

        List<Plan> plans = planService.findAll();

        AppResponse<List<Plan>> response = new AppResponse<>(
                "success",
                plans,
                200,
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
