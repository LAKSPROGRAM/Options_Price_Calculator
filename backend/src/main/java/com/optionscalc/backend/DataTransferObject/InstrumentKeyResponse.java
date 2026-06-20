package com.optionscalc.backend.DataTransferObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentKeyResponse {
    private String instrumentkey;
    private String tradingSymbol;
    private String expiry;
    private long daystoexpiry;
}
