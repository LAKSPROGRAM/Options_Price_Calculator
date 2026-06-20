package com.optionscalc.backend.service;
import com.optionscalc.backend.DataTransferObject.InstrumentKeyResponse;
import com.optionscalc.backend.exception.UpstoxApiException;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service

public class InstrumentLocatorService {
    private static final String MASTER_CSV_URL=
            "https://assets.upstox.com/market-quote/instruments/exchange/complete.csv.gz";
    public InstrumentKeyResponse locateKey(String symbol,double strike,String optType){
        List<Map<String,String>> rows;
        try {
            rows=downloadAndParseCsv();
        } catch (Exception e){
            throw new UpstoxApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not download upstox instrument master" + e.getMessage());
        }
        String tsCol=null;
        if(!rows.isEmpty()){
            for(String col:rows.get(0).keySet()){
                String lower=col.toLowerCase().trim();
                if(lower.contains("trading") && lower.contains("symbol")){
                    tsCol=col;
                    break;
                }
            }
        }
        if(tsCol==null){
            throw new UpstoxApiException(HttpStatus.INTERNAL_SERVER_ERROR,
            "Cannot find tradingsymbol column in instrument master CSV.");
        }
        String strikeStr=formatStrike(strike);
        LocalDate today=LocalDate.now();
        InstrumentKeyResponse best=null;
        LocalDate bestExpiry =null;
        for(Map<String,String> row:rows){
            String name=row.getOrDefault("name","").trim();
            String instrumentType=row.getOrDefault("instrument_type","").trim();
            String rowStrike=row.getOrDefault("strike","").trim();
            String tradingSymbol=row.getOrDefault(tsCol,"").trim();
            String expiryStr=row.getOrDefault("expiry","").trim();
            String instrumentKey=row.getOrDefault("instrument_key","").trim();

            if(!symbol.equalsIgnoreCase(name)) continue;
            if(!"OPTIDX".equalsIgnoreCase(instrumentType)) continue;
            if(!matchesStrike(rowStrike,strikeStr)) continue;
            if(!tradingSymbol.toUpperCase().endsWith(optType.toUpperCase())) continue;

            LocalDate expiry=parseExpiry(expiryStr);
            if(expiry==null || expiry.isBefore(today)) {
                continue;
            }
                if (bestExpiry == null || expiry.isBefore(bestExpiry)){
                bestExpiry=expiry;
                long dte=java.time.temporal.ChronoUnit.DAYS.between(today,expiry);
                best= new InstrumentKeyResponse(
                        instrumentKey,
                        tradingSymbol,
                        expiry.toString(),
                        Math.max(dte,0)
                );
            }
        }
        if(best==null){
            throw new UpstoxApiException(HttpStatus.NOT_FOUND,
                    "No active OPTIDX contract found for" + symbol+" "+ strikeStr+" "+optType+
                    ". Check that strike is a valid Nifty strike(multiples of 50) or contract may have expired.");

        }
        return best;
    }
    private List<Map<String,String>> downloadAndParseCsv() throws Exception{
        URL url=URI.create(MASTER_CSV_URL).toURL();
        try (InputStream rawIn =url.openStream();
            GzipCompressorInputStream gzpIn =new GzipCompressorInputStream(rawIn);
            BufferedReader reader=new BufferedReader(new InputStreamReader(gzpIn,StandardCharsets.UTF_8))){
            CSVFormat format=CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build();
            List<Map<String ,String>> result=new ArrayList<>();
            for(CSVRecord record:format.parse(reader)){
                Map<String,String> rowMap=new HashMap<>();
                record.toMap().forEach((k,v)->rowMap.put(k.toLowerCase().trim(),v));
                result.add(rowMap);
            }
            return result;
        }
    }
    private boolean matchesStrike(String rowStrike,String targetStrike){
        try{
            double a =Double.parseDouble(rowStrike);
            double b =Double.parseDouble(targetStrike);
            return Math.abs(a-b)<0.001;
        } catch (NumberFormatException e){
            return false;
        }
    }
    private String formatStrike(double strike){
        if(strike==Math.floor(strike)){
            return String.valueOf((long)strike);
        }
        return String.valueOf(strike);
    }
    private LocalDate parseExpiry(String expiryStr){
        if(expiryStr==null || expiryStr.isBlank()) return null;
        String[] patterns={"yyyy-MM-dd","dd-MM-yyyy","yyyy/MM/dd"};
        for(String pattern :patterns){
            try{
                return LocalDate.parse(expiryStr,DateTimeFormatter.ofPattern(pattern));
            } catch(Exception ignored){
                //try next Pattern
            }
        }
        try{
            long epochMillis=Long.parseLong(expiryStr);
            return java.time.Instant.ofEpochMilli(epochMillis)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        } catch(NumberFormatException ignored){
            return null;
        }
    }
}
