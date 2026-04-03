package com.aegis.saas.controller;

import com.aegis.saas.dto.AppResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.aegis.saas.entity.Plan;
import com.aegis.saas.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Public - Health & Plans", description = "Public endpoints: health check and engine plan listing. No authentication required.")
@RestController
@RequestMapping("/api/v1/health")
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
