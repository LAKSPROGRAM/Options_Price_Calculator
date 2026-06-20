package com.optionscalc.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optionscalc.backend.DataTransferObject.MarketData;
import com.optionscalc.backend.exception.UpstoxApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.DefaultUriBuilderFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;

@Service
public class UpstoxService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper=new ObjectMapper();

    @Autowired
    public UpstoxService(WebClient.Builder webClientBuilder){
        this.webClient=webClientBuilder
                .baseUrl("https://api.upstox.com")
                .build();
    }
    public MarketData fetchMarketData(String instrumentKey, String accessToken) {

        // Forcefully strip any hidden spaces, tabs, or newlines from copy-pasting!
        String cleanToken = accessToken.trim();

        // Combine option key + Nifty spot index
        String combinedKeys = instrumentKey + ",NSE_INDEX|Nifty 50";

        String responseBody;
        try {
            responseBody = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/market-quote/quotes")
                            .queryParam("instrument_key", combinedKeys)
                            .build())
                    .header("Accept", "application/json")
                    // Use the cleanToken here instead of the raw accessToken
                    .header("Authorization", "Bearer " + cleanToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch(WebClientResponseException e){
            if(e.getStatusCode()==HttpStatus.UNAUTHORIZED){
                throw new UpstoxApiException(HttpStatus.UNAUTHORIZED,
                        "Token rejected by upstox .it may have expired");
            }
            throw new UpstoxApiException(HttpStatus.valueOf(e.getStatusCode().value()),
                    "Upstox API error: "+e.getResponseBodyAsString());
        }catch(Exception e){
            throw new UpstoxApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Network error connecting to upstox"+e.getMessage());
        }
        JsonNode root;
        try{
            root=objectMapper.readTree(responseBody);
        } catch(Exception e){
            throw new UpstoxApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to parse Upstox response: "+e.getMessage());
        }
        JsonNode data=root.get("data");
        if(data==null || !data.fieldNames().hasNext()){
            throw new UpstoxApiException(HttpStatus.NOT_FOUND,
                    "Upstox returned empty data.Instrument key may be wrong or expired.");

        }
        double premium =0.0;
        double spot=0.0;
        long volume=0L;
        Iterator<Map.Entry<String,JsonNode>> fields=data.fields();
        while(fields.hasNext()){
            Map.Entry<String,JsonNode> entry=fields.next();
            String key=entry.getKey().toLowerCase();
            JsonNode val=entry.getValue();
            if(key.contains("nifty 50") || key.contains("nifty50")){
                spot=getDouble(val,"last_price","ltp");
            }else{
                premium=getDouble(val,"last_price","ltp");
                volume=val.has("volume") ? val.get("volume").asLong():0L;
            }
        }
        if(premium==0.0){
            throw new UpstoxApiException(HttpStatus.NOT_FOUND,
                    "Got a response but option premium is 0. The instrument key may be wrong " +
                            "or the contract has expired. Use /api/locate-key to get today's fresh key.");
        }
        String timestamp=LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new MarketData(premium,spot,volume,timestamp);
    }
    private double getDouble(JsonNode node,String... fieldNames){
        for(String field:fieldNames){
            if(node.has(field)&& !node.get(field).isNull()){
                return node.get(field).asDouble();
            }
        }
        return 0.0;
    }
}
