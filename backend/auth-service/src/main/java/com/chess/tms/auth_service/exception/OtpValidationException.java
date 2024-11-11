package com.chess.tms.auth_service.exception;

public class OtpValidationException extends RuntimeException {
    public OtpValidationException(String message) {
        super(message);
    }
}
