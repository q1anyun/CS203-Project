package com.chess.tms.tournament_service.exception;

public class MatchServiceException extends RuntimeException {
    public MatchServiceException(String message) {
        super(message);
    }

    public MatchServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}