package com.aegis.saas.controller;

import com.aegis.saas.dto.AppResponse;
import com.aegis.saas.dto.PaginatedResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.aegis.saas.dto.PlanRequest;
import com.aegis.saas.entity.Plan;
import com.aegis.saas.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Super Admin - Engine Plans", description = "Create and manage the infrastructure-level pricing plans (FREE, PRO, ENTERPRISE)")
@RestController
@RequestMapping("/api/v1/super-admin/engine-plans")
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
    public ResponseEntity<AppResponse<PaginatedResponse<Plan>>> getAllPlans(Pageable pageable) {
        Page<Plan> page = planService.findAllPaginated(pageable);
        PaginatedResponse<Plan> data = PaginatedResponse.from(page);

        return ResponseEntity.ok(
                new AppResponse<>("Success", data, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }

    @GetMapping("/all")
    public ResponseEntity<AppResponse<List<Plan>>> getAllPlansNoPagination() {
        List<Plan> data = planService.findAll();

        return ResponseEntity.ok(
                new AppResponse<>("Success", data, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }
}

