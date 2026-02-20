package com.jobhunt.saas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegRequest {

    @NotBlank(message = "Tenant Name Cannot Be Blank")
    private String tenantName;

    @NotBlank(message = "name Cannot be Blank")
    private String userName;

    @NotBlank(message = "Email Cannot be Blank")
    @Email(message = "provide a Valid Email")
    private String email;

    @NotBlank
    @Size(min = 2, max = 20,message = "Password must be between 8 and 20 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;
}
