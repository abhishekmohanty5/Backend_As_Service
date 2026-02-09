package com.jobhunt.saas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppResponse<T> {
    private String message;
    private T data;
    private int status;
    private LocalDateTime timestamp;
}
