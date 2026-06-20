package com.optionscalc.backend.DataTransferObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketData {
    private double livePremium;
    private double spotPrice;
    private long volume;
    private String Timestamp;
}
