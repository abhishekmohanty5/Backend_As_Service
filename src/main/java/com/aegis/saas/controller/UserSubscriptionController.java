package com.aegis.saas.controller;

import com.aegis.saas.dto.AppResponse;
import com.aegis.saas.dto.PaginatedResponse;
import com.aegis.saas.dto.SubscriptionStatsDto;
import com.aegis.saas.dto.UserSubscriptionDto;
import com.aegis.saas.entity.UserSubscription;
import com.aegis.saas.service.UserSubscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

@Tag(name = "Tenant User Subscriptions", description = "Tenant Admin manages end-user subscriptions")
@RestController
@RequestMapping("/api/v1/tenant-admin/user-subscriptions")
@RequiredArgsConstructor
public class UserSubscriptionController {


        private final UserSubscriptionService userSubscriptionService;

        // create Subscriptions
        @PostMapping
        public ResponseEntity<AppResponse<Void>> addNewSubscription(
                        @Valid @RequestBody UserSubscriptionDto userSubscriptionDto) {

                userSubscriptionService.createSubscription(userSubscriptionDto);

                AppResponse<Void> response = AppResponse.<Void>builder()
                                .message("Successfully added subscription")
                                .status(HttpStatus.CREATED.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @GetMapping
        public ResponseEntity<AppResponse<PaginatedResponse<UserSubscription>>> getAllSubscriptions(Pageable pageable) {
                Page<UserSubscription> subscriptions = userSubscriptionService.getUserSubscriptionsPaginated(pageable);
                PaginatedResponse<UserSubscription> data = PaginatedResponse.from(subscriptions);

                AppResponse<PaginatedResponse<UserSubscription>> response = AppResponse.<PaginatedResponse<UserSubscription>>builder()
                                .message("Success")
                                .data(data)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/all")
        public ResponseEntity<AppResponse<List<UserSubscription>>> getAllSubscriptionsNoPagination() {
                List<UserSubscription> subscriptions = userSubscriptionService.getUserSubscriptions();

                AppResponse<List<UserSubscription>> response = AppResponse.<List<UserSubscription>>builder()
                                .message("Success")
                                .data(subscriptions)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/active")
        public ResponseEntity<AppResponse<List<UserSubscription>>> getActiveSubscription() {

                List<UserSubscription> subscriptions = userSubscriptionService.getActiveSubscription();

                AppResponse<List<UserSubscription>> response = AppResponse.<List<UserSubscription>>builder()
                                .data(subscriptions)
                                .timestamp(LocalDateTime.now())
                                .message("Success")
                                .status(HttpStatus.OK.value())
                                .build();

                return ResponseEntity.ok(response);

        }

        @GetMapping("/tenant-plan/{id}")
        public ResponseEntity<AppResponse<List<UserSubscription>>> getSubscriptionByTenantPlan(
                        @PathVariable Long id) {

                List<UserSubscription> data = userSubscriptionService.getSubscriptionByTenantPlan(id);

                AppResponse<List<UserSubscription>> response = AppResponse.<List<UserSubscription>>builder()
                                .message("Success")
                                .timestamp(LocalDateTime.now())
                                .data(data)
                                .status(HttpStatus.OK.value())
                                .build();

                return ResponseEntity.ok(response);
        }

        @PutMapping("/update/{id}")
        public ResponseEntity<AppResponse<Void>> updateSubscription(
                        @PathVariable Long id,
                        @Valid @RequestBody UserSubscriptionDto userSubscriptionDto) {

                userSubscriptionService.updateSubscription(id, userSubscriptionDto);

                AppResponse<Void> response = AppResponse.<Void>builder()
                                .message("Success")
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.OK.value())
                                .data(null)
                                .build();

                return ResponseEntity.ok(response);
        }

        // Cancel Subscription

        @PutMapping("/cancel/{id}")
        public ResponseEntity<AppResponse<String>> cancelSubscription(@PathVariable Long id) {
                userSubscriptionService.cancelSubscription(id);

                AppResponse<String> response = AppResponse.<String>builder()
                                .message("Success")
                                .data("Subscription Cancel SuccessFully With Id" + id)
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.OK.value())
                                .build();

                return ResponseEntity.ok(response);
        }

        // Renewal in next N days
        @GetMapping("/upcoming")
        public ResponseEntity<AppResponse<List<UserSubscription>>> getUpcomingRenewalsSubscriptions(
                        @RequestParam(defaultValue = "7") int days) {

                List<UserSubscription> subscriptions = userSubscriptionService.getUpcomingRenewals(days);

                AppResponse<List<UserSubscription>> response = AppResponse.<List<UserSubscription>>builder()
                                .timestamp(LocalDateTime.now())
                                .data(subscriptions)
                                .message("Success")
                                .status(HttpStatus.OK.value())
                                .build();

                return ResponseEntity.ok(response);

        }

        @GetMapping("/stats")
        public ResponseEntity<AppResponse<SubscriptionStatsDto>> getSubscriptionStatsDto() {

                SubscriptionStatsDto stats = userSubscriptionService.getSubscriptionStatistics();

                AppResponse<SubscriptionStatsDto> response = AppResponse.<SubscriptionStatsDto>builder()
                                .message("Success")
                                .data(stats)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/insights")
        public ResponseEntity<AppResponse<List<String>>> getSubscriptionInsights() {
                List<String> data = userSubscriptionService.getSubscriptionInsights();

                AppResponse<List<String>> response = AppResponse.<List<String>>builder()
                                .data(data)
                                .message("Success")
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.OK.value())
                                .build();

                return ResponseEntity.ok(response);
        }

}
