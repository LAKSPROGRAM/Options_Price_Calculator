package com.optionscalc.backend.DataTransferObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class GreeksRequest extends MarketDataRequest {
    @NotNull(message="strike is required")
    private double strike;
    @NotNull(message="Days to remain expiry")
    private Double daysToExpiry;
    private Double riskFreeRate=0.07;
    private String optionType="CE";
}
