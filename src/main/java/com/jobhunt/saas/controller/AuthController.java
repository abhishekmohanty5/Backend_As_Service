package com.jobhunt.saas.controller;

import com.jobhunt.saas.dto.AppResponse;
import com.jobhunt.saas.dto.LoginRequest;
import com.jobhunt.saas.dto.LoginResponse;
import com.jobhunt.saas.dto.RegistrationRequest;
import com.jobhunt.saas.dto.RegistrationResponse;
import com.jobhunt.saas.service.AuthService;
import com.jobhunt.saas.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    AuthService authService;

    @PostMapping("/reg")
    public ResponseEntity<AppResponse<RegistrationResponse>> regUser(@Valid @RequestBody RegistrationRequest registrationRequest){
      RegistrationResponse response= userService.addUser(registrationRequest);
      AppResponse<RegistrationResponse> appResponse =
              new AppResponse<>("Success",response,200, LocalDateTime.now());
      return ResponseEntity.ok(appResponse);
    }
    @PostMapping("/log")
    public ResponseEntity<AppResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest){
        LoginResponse response= authService.login(loginRequest);
        AppResponse<LoginResponse> body=
                new AppResponse<>("Success",response,200, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }
}
