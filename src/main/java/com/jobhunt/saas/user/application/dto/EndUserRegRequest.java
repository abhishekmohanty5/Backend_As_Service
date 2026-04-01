package com.jobhunt.saas.user.application.dto;

import lombok.Data;

@Data
public class EndUserRegRequest {
    private String name;
    private String email;
    private String password;
}
