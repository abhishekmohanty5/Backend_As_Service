package com.jobhunt.saas.controller;

import com.jobhunt.saas.dto.AppResponse;
import com.jobhunt.saas.dto.PlanRequest;
import com.jobhunt.saas.entity.Plan;
import com.jobhunt.saas.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/plan")
public class PlanController {


    private  final PlanService planService;

    @Autowired
    public PlanController(PlanService planService) {
        this.planService = planService;
    }


    @PostMapping
    public ResponseEntity<AppResponse<String>> createPlan(
            @Valid @RequestBody PlanRequest planRequest) {

        planService.createPlan(planRequest);

        AppResponse<String> response = new AppResponse<>(
                "success",
                "Plan created",
                201,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<AppResponse<String>> activatePlan(@PathVariable Long id) {

        planService.activatePlan(id);

        return ResponseEntity.ok(
                new AppResponse<>("success", "Plan activated", 200, LocalDateTime.now())
        );
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<AppResponse<String>> deactivatePlan(@PathVariable Long id) {

        planService.deactivatePlan(id);

        return ResponseEntity.ok(
                new AppResponse<>("success", "Plan deactivated", 200, LocalDateTime.now())
        );
    }

    @GetMapping
    public ResponseEntity<AppResponse<List<Plan>>> getAllPlans() {
        List<Plan> data = planService.findAll();

        return ResponseEntity.ok(
                new AppResponse<>("Success",data,HttpStatus.OK.value(),LocalDateTime.now())
        );
    }
}

