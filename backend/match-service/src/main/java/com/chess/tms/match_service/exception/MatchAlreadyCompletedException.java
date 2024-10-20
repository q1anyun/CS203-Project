package com.chess.tms.match_service.exception;

public class MatchAlreadyCompletedException extends RuntimeException {
    public MatchAlreadyCompletedException(String message) {
        super(message);
    }
    
}
