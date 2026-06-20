package com.optionscalc.backend.DataTransferObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Greeks {
    private double delta;
    private double gamma;
    private double theta;
    private double vega;
    private double rho;
    private double iv; // as percentage, e.g. 18.45
}
