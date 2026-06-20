package com.optionscalc.backend.service;


import com.optionscalc.backend.DataTransferObject.Greeks;
import com.optionscalc.backend.DataTransferObject.MarketData;
import com.optionscalc.backend.DataTransferObject.PredictRequest;
import com.optionscalc.backend.DataTransferObject.PredictResponse;
import org.springframework.stereotype.Service;

@Service
public class PredictionService {
    private final UpstoxService upstoxService;
    private final OptionsMathService mathService;

    public PredictionService(UpstoxService upstoxService,OptionsMathService mathService) {
        this.upstoxService = upstoxService;
        this.mathService = mathService;
    }
    public PredictResponse predict(PredictRequest req) {
        MarketData data=upstoxService.fetchMarketData(req.getInstrumentKey(),req.getAccessToken());

        double t=Math.max(req.getDaysToExpiry()/365.0,0.001);
        Greeks g=mathService.calculateGreeks(
                data.getSpotPrice(),
                req.getStrike(),
                t,
                req.getRiskFreeRate(),
                data.getLivePremium(),
                req.getOptionType()
        );
        double spotChange=req.getTargetSpot()-data.getSpotPrice();
        double daysPassed =req.getMinutesUntilTarget()/(24.0 *60.0);

        double deltaPnl=g.getDelta()*spotChange;
        double gammaPnl=0.5*g.getGamma()*(spotChange*spotChange);
        double thetaPnl=g.getTheta()*daysPassed;
        double predicted=data.getLivePremium()+deltaPnl+gammaPnl+thetaPnl;

        Double breakeven=(g.getDelta()!=0) ? Math.abs(data.getLivePremium()/g.getDelta()):null;
        double vega5pct=g.getVega()*5;

        String sentiment;
        if(g.getIv()>25){
            sentiment="High Volatility-Wide spread expected";
        }else if(g.getIv()>18){
            sentiment="Moderate Volatility-Normal condition";
        }else{
            sentiment="Low Volatility-Possible breakout ahead";
        }
        return PredictResponse.builder()
                .livePremium(data.getLivePremium())
                .spotPrice(data.getSpotPrice())
                .volume(data.getVolume())
                .timestamp(data.getTimestamp())
                .delta(g.getDelta())
                .gamma(g.getGamma())
                .theta(g.getTheta())
                .vega(g.getVega())
                .rho(g.getRho())
                .iv(g.getIv())
                .targetSpot(req.getTargetSpot())
                .minutes(req.getMinutesUntilTarget())
                .spotChange(round(spotChange, 2))
                .deltaPnl(round(deltaPnl, 2))
                .gammaPnl(round(gammaPnl, 2))
                .thetaPnl(round(thetaPnl, 2))
                .predictedPremium(round(predicted, 2))
                .breakevenMove(breakeven != null ? round(breakeven, 2) : null)
                .vega5pctImpact(round(vega5pct, 2))
                .ivSentiment(sentiment)
                .build();
    }
    private double round(double value, int places) {
        double factor=Math.pow(10,places);
        return Math.round(value*factor)/factor;
    }
}
