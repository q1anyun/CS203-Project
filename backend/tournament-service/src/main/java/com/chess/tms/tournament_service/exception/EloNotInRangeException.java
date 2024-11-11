package com.chess.tms.tournament_service.exception;

public class EloNotInRangeException extends RuntimeException{
    public EloNotInRangeException(String message) {
        super(message);
    }
}
