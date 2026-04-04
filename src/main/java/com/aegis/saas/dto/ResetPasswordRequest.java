package com.aegis.saas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    private String token;

    @NotBlank(message = "Password is required")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}
