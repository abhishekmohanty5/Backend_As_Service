package com.aegis.saas.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanRequest {
    @NotNull(message = "Provide a Valid Plan Name")
     private String name;
    @NotNull(message = "provide a Valid Plan Price")
     private BigDecimal price;
    @NotNull(message = "provide a valid Duration")
     private int durationInDays;
}
