package com.chess.tms.match_service.exception;

public class GameTypeNotFoundException extends RuntimeException {
    public GameTypeNotFoundException(String message) {
        super(message);
    }
}
