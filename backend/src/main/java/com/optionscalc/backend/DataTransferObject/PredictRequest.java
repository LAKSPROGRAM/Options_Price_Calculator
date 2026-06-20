package com.optionscalc.backend.DataTransferObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class PredictRequest extends GreeksRequest {
    @NotNull(message="target spot")
    private Double targetSpot;
    @NotNull(message="minutes until target")
    private Integer minutesUntilTarget;
}
