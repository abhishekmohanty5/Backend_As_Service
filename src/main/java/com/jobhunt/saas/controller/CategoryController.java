package com.jobhunt.saas.controller;

import com.jobhunt.saas.dto.AppResponse;
import com.jobhunt.saas.dto.SubscriptionCategoryDto;
import com.jobhunt.saas.service.SubscriptionCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final SubscriptionCategoryService subscriptionCategoryService;


    @GetMapping
    public ResponseEntity<AppResponse<List<SubscriptionCategoryDto>>> getAllCategories() {

        List<SubscriptionCategoryDto> categories =
                subscriptionCategoryService.getAllSubscriptionCategories();

        AppResponse<List<SubscriptionCategoryDto>> response =
                AppResponse.<List<SubscriptionCategoryDto>>builder()
                        .message("Success")
                        .data(categories)
                        .status(HttpStatus.OK.value())
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AppResponse<SubscriptionCategoryDto>> createCategory(
            @Valid @RequestBody SubscriptionCategoryDto category) {

        SubscriptionCategoryDto  data =subscriptionCategoryService.createSubscriptionCategory(category);

        AppResponse<SubscriptionCategoryDto> response =
                AppResponse.<SubscriptionCategoryDto>builder()
                        .message("Category created successfully")
                        .data(data)
                        .status(HttpStatus.CREATED.value())
                        .timestamp(LocalDateTime.now())
                        .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
