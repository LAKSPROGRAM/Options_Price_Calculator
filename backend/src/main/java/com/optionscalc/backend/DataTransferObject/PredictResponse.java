package com.optionscalc.backend.DataTransferObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PredictResponse {
//live
    private Double livePremium;
    private Double spotPrice;
    private String timestamp;
    private long volume;

    //greeks
    private Double gamma;
    private Double delta;
    private Double iv;
    private Double theta;
    private Double vega;
    private double rho;

    //prediction
        private double targetSpot;
        private int minutes;
        private double spotChange;
        private double deltaPnl;
        private double gammaPnl;
        private double thetaPnl;
        private double predictedPremium;
        private Double breakevenMove; // nullable
        private double vega5pctImpact;
        private String ivSentiment;
}
