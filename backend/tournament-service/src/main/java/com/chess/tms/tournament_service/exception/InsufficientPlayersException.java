package com.chess.tms.tournament_service.exception;

public class InsufficientPlayersException extends RuntimeException {
    public InsufficientPlayersException(String message) {
        super(message);
    }
    
}
