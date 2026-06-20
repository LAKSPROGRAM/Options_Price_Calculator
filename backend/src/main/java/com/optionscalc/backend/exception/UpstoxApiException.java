package com.optionscalc.backend.exception;

import org.springframework.http.HttpStatus;

public class UpstoxApiException  extends RuntimeException {
    private final HttpStatus status;

    public UpstoxApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

}
