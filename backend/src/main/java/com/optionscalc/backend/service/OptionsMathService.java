package com.optionscalc.backend.service;


import com.optionscalc.backend.DataTransferObject.Greeks;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.stereotype.Service;

@Service

public class OptionsMathService {
    private static final NormalDistribution NORMAL=new NormalDistribution();
    private static final int MAX_ITERATIONS=100;
    private static final double PRECISION=1.0e-5;

    public double impliedVolatility(double s,double k,double t, double r,double marketPrice,String optionType){
        double sigma=0.20; // initial guess=20%
        for(int i=0;i<MAX_ITERATIONS;i++){
            if(sigma<=0){
                sigma=1e-6;
            }
            double d1=(Math.log(s / k)+(r+0.5*sigma*sigma) *t) / (sigma * Math.sqrt(t));
            double d2=d1-sigma*Math.sqrt(t);
            double price;
            if("CE".equalsIgnoreCase(optionType)){
                price=s*cdf(d1)-k*Math.exp(-r*t) * cdf(d2);
            }else{
                price=k*Math.exp(-r*t) * cdf(-d2)-s*cdf(-d1);
            }
            double diff=marketPrice-price;
            if(Math.abs(diff)<PRECISION){
                return sigma;
            }
            double vega=s*pdf(d1) * Math.sqrt(t);
            if(vega<1e-8){
                break;
            }
            sigma+=diff/vega;
        }
        return sigma;
    }
    public Greeks calculateGreeks(double s,double k,double t,double r,double livePremium,String optionType){
        double sigma=impliedVolatility(s,k,t,r,livePremium,optionType);
        if(t<=0){
            t=0.001;
        }
        double d1=(Math.log(s/k)+(r+0.5*sigma*sigma)*t) / (sigma*Math.sqrt(t));
        double d2=d1-sigma*Math.sqrt(t);

        double gamma=pdf(d1)/(s*sigma*Math.sqrt(t));
        double vega=s*pdf(d1) * Math.sqrt(t)/100;

        double delta;
        double theta;
        double rho;

        if("CE".equalsIgnoreCase(optionType)){
            delta=cdf(d1);
            theta=(-(s*pdf(d1)*sigma) / (2*Math.sqrt(t))
            -r*k*Math.exp(-r*t)*cdf(d2)) / 365;
            rho = k*t*Math.exp(-r*t)*cdf(d2)/100;
        }else{
            delta=cdf(d1)-1;
            theta=(-(s*pdf(d1) * sigma) / (2*Math.sqrt(t)) + r*k*Math.exp(-r*t) *cdf(-d2))/365;
            rho=-k*t*Math.exp(-r*t)*cdf(-d2)/100;
        }
        return new Greeks(
                round(delta,4),
                round(gamma,6),
                round(theta,4),
                round(vega,4),
                round(rho,4),
                round(sigma*100,2)
        );
    }
    private double cdf(double x){
        return NORMAL.cumulativeProbability(x);
    }
    private double pdf(double x){
        return NORMAL.density(x);
    }
    private double round(double value,int places){
        double factor=Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }
}
