package com.jobhunt.saas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionCategoryDto {

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String icon;

    private LocalDate createdDate;

}
