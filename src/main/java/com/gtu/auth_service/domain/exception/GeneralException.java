package com.gtu.auth_service.domain.exception;

public class GeneralException extends RuntimeException {
    private final int statusCode;
    private final String message;

    public GeneralException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
    
}