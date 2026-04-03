package com.aegis.saas.controller;

import com.aegis.saas.dto.AppResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.aegis.saas.dto.LoginRequest;
import com.aegis.saas.dto.LoginResponse;
import com.aegis.saas.dto.RegistrationRequest;
import com.aegis.saas.dto.RegistrationResponse;
import com.aegis.saas.service.AuthService;
import com.aegis.saas.service.UserService;
import jakarta.persistence.GeneratedValue;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Auth - Tenant Registration", description = "Tenant company registration, login and email verification")
@RestController
@RequestMapping("/api/v1/auth")
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
