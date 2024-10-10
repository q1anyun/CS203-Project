package com.chess.tms.tournament_service.exception;

public class GameTypeNotFoundException extends RuntimeException{
    public GameTypeNotFoundException(String message) {
        super(message);
    }
}
