package com.optionscalc.backend.controller;
import com.optionscalc.backend.DataTransferObject.*;
import com.optionscalc.backend.DataTransferObject.Greeks;
import com.optionscalc.backend.service.InstrumentLocatorService;
import com.optionscalc.backend.service.OptionsMathService;
import com.optionscalc.backend.service.PredictionService;
import com.optionscalc.backend.service.UpstoxService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OptionsController {
    private final UpstoxService upstoxService;
    private final PredictionService predictionService;
    private final OptionsMathService mathService;
    private final InstrumentLocatorService locatorService;
    public OptionsController(UpstoxService upstoxService,OptionsMathService mathService,InstrumentLocatorService locatorService,PredictionService predictionService){
        this.upstoxService=upstoxService;
        this.mathService=mathService;
        this.locatorService=locatorService;
        this.predictionService=predictionService;
    }
    @GetMapping("/health")
    public Map<String,String> health(){
        Map<String,String> body=new HashMap<>();
        body.put("status","ok");
        body.put("time",LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return body;
    }
    @PostMapping("/market-data")
    public MarketData getMarketData(@Valid @RequestBody MarketDataRequest req){
        return upstoxService.fetchMarketData(req.getInstrumentKey(),req.getAccessToken());
    }
    @PostMapping("/greeks")
    public Map<String, Object> getGreeks(@Valid @RequestBody GreeksRequest req) {
        MarketData data = upstoxService.fetchMarketData(req.getInstrumentKey(), req.getAccessToken());
        double t = Math.max(req.getDaysToExpiry() / 365.0, 0.001);
        Greeks g = mathService.calculateGreeks(
                data.getSpotPrice(), req.getStrike(), t,
                req.getRiskFreeRate(), data.getLivePremium(), req.getOptionType()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("live_premium", data.getLivePremium());
        result.put("spot_price", data.getSpotPrice());
        result.put("volume", data.getVolume());
        result.put("timestamp", data.getTimestamp());
        result.put("delta", g.getDelta());
        result.put("gamma", g.getGamma());
        result.put("theta", g.getTheta());
        result.put("vega", g.getVega());
        result.put("rho", g.getRho());
        result.put("iv", g.getIv());
        return result;
    }
    @PostMapping("/predict")
    public PredictResponse predict(@Valid @RequestBody PredictRequest req){
        return predictionService.predict(req);
    }
    @GetMapping("/locate-key")
    public InstrumentKeyResponse locateKey(
            @RequestParam(defaultValue="NIFTY") String symbol,
            @RequestParam(defaultValue="24000") double strike,
            @RequestParam(defaultValue="CE") String optType
    ){
        return locatorService.locateKey(symbol,strike,optType);
    }
    @PostMapping("/debug-raw")
    public MarketData debugRaw(@Valid @RequestBody MarketDataRequest req){
        return upstoxService.fetchMarketData(req.getInstrumentKey(),req.getAccessToken());
    }
}
