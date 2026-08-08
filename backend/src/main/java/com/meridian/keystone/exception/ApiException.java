package com.meridian.keystone.exception;

import org.springframework.http.HttpStatus;

/** Domain/API exception carrying an HTTP status and a structured message. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}