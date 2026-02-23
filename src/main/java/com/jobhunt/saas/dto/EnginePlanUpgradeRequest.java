package com.jobhunt.saas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnginePlanUpgradeRequest {
    private Long targetPlanId;
}
