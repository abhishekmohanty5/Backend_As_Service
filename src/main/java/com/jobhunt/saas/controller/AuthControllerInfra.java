package com.jobhunt.saas.controller;

import com.jobhunt.saas.dto.AppResponse;
import com.jobhunt.saas.dto.LoginRequest;
import com.jobhunt.saas.dto.LoginResponse;
import com.jobhunt.saas.dto.RegistrationRequest;
import com.jobhunt.saas.dto.RegistrationResponse;
import com.jobhunt.saas.service.AuthService;
import com.jobhunt.saas.service.UserService;
import jakarta.persistence.GeneratedValue;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthControllerInfra {

    @Autowired
    private UserService userService;
    @Autowired
    AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AppResponse<RegistrationResponse>> regUser(@Valid @RequestBody RegistrationRequest registrationRequest){
      RegistrationResponse response= userService.addUser(registrationRequest);
      AppResponse<RegistrationResponse> appResponse =
              new AppResponse<>("Success",response,200, LocalDateTime.now());
      return ResponseEntity.ok(appResponse);
    }
    @PostMapping("/login")
    public ResponseEntity<AppResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest){
        LoginResponse response= authService.login(loginRequest);
        AppResponse<LoginResponse> body=
                new AppResponse<>("Success",response,200, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<AppResponse<String>> verifyEmail(@RequestParam String token){

        authService.verifyEmail(token);
        AppResponse<String> body=
                new AppResponse<>("Email Verified Successfully",
                        null,
                        200,
                        LocalDateTime.now()
                );
         return ResponseEntity.ok(body);
    }
}
