package com.jobhunt.saas.controller;

import com.jobhunt.saas.dto.AppResponse;
import com.jobhunt.saas.dto.SubscriptionResponse;
import com.jobhunt.saas.entity.Subscription;
import com.jobhunt.saas.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {


    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe/{id}")
    public ResponseEntity<AppResponse<SubscriptionResponse>> subscribe(@PathVariable Long id) {
        SubscriptionResponse response= subscriptionService.subscribe(id);
        AppResponse<SubscriptionResponse> responseDto= new AppResponse<>(
                "success",
                response,
                200,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PutMapping("/cancel")
    public ResponseEntity<AppResponse<String>> cancel(){
        subscriptionService.cancelSubscription();
        AppResponse<String> response = new AppResponse<>(
                "success",
                "Subscription Cancelled",
                200,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping()
    public ResponseEntity<AppResponse<Subscription>> getSubscription()
    {
        Subscription subscription=subscriptionService.getCurrentUserActiveSubscription();
        AppResponse<Subscription> response = new AppResponse<>(
                "success",
                subscription,
                200,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
