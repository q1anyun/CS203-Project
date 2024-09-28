package com.chess.tms.tournament_service.exception;

public class RoundTypeNotFoundException extends RuntimeException {
    public RoundTypeNotFoundException(String message) {
        super(message);
    }
}