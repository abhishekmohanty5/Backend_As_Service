package com.jobhunt.saas.controller;

import com.jobhunt.saas.dto.AppResponse;
import com.jobhunt.saas.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestBackendController {

   private final EmailService emailService;


   @GetMapping("/health")
   public ResponseEntity<String> testHealth() {

       return ResponseEntity.status(HttpStatus.OK).body("Backend Run Normally");
    }
   @GetMapping("/email")
    public ResponseEntity<AppResponse<String>> testEmail(){

        emailService.sendEmail("2470033@kiit.ac.in","Check Email Module","Email Sent SuccessFully");

        AppResponse<String>  response = AppResponse.<String>builder()
                .message("Success")
                .status(HttpStatus.OK.value())
                .data("Message sent success Fully")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

}
