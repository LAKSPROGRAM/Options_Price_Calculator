package com.optionscalc.backend.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UpstoxApiException.class)
    public ResponseEntity<Map<String,String>> handleUpstoxApiException(UpstoxApiException ex){
        Map<String ,String>body=new HashMap<>();
        body.put("detail",ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidateException(MethodArgumentNotValidException ex){
        Map<String ,String>body=new HashMap<>();
        String messgae=ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe->fe.getField()+": "+fe.getDefaultMessage())
                .orElse("Validation failed");
        body.put("detail",messgae);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> handleGenericException(Exception ex){
        Map<String ,String>body=new HashMap<>();
        body.put("detail","Internal error"+ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
