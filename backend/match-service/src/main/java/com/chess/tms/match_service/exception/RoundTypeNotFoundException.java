package com.chess.tms.match_service.exception;

public class RoundTypeNotFoundException extends RuntimeException {
    public RoundTypeNotFoundException(String message) {
        super(message);
    }
}