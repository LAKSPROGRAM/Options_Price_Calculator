package com.optionscalc.backend.DataTransferObject;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
@Data
public class MarketDataRequest {
    @NotBlank(message="instrument key required")
    private String instrumentKey;
    @NotBlank(message="access token is required")
    private String accessToken;
}
